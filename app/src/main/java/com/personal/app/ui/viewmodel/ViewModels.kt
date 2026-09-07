package com.personal.app.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.app.AppContainer
import com.personal.app.LocalAppContainer
import com.personal.app.data.bank.Institution
import com.personal.app.data.model.Account
import com.personal.app.data.model.AccountType
import com.personal.app.data.model.BankConnection
import com.personal.app.data.model.Category
import com.personal.app.data.model.Money
import com.personal.app.data.model.Transaction
import com.personal.app.data.prefs.PreferencesRepository
import com.personal.app.data.prefs.ThemeMode
import com.personal.app.data.prefs.UserPreferences
import com.personal.app.data.repository.FinanceRepository
import com.personal.app.data.repository.SyncState
import com.personal.app.domain.FinanceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

/*
 * ViewModels are thin: they turn repository flows into screen states and forward intents.
 * They get the repository from the composition (LocalAppContainer), so previews and tests can
 * inject an in-memory one.
 */

@Composable
inline fun <reified VM : ViewModel> appViewModel(crossinline create: (AppContainer) -> VM): VM {
    val container = LocalAppContainer.current
    return viewModel { create(container) }
}

// ---- Home ----

data class HomeUiState(
    val userName: String = "",
    val todayMillis: Long = 0,
    val pendingCaptures: Int = 0,
    val accounts: List<Account> = emptyList(),
    val totalMinor: Long = 0,
    val monthOverMonthPercent: Double? = null,
    val expenseShares: List<Pair<Category, Float>> = emptyList(),
    val recent: List<Transaction> = emptyList(),
    val hasData: Boolean = false,
)

class HomeViewModel(private val repo: FinanceRepository, private val prefs: PreferencesRepository) : ViewModel() {
    val state: StateFlow<HomeUiState> = combine(repo.data, prefs.prefs) { d, p ->
        val month = YearMonth.from(Instant.ofEpochMilli(repo.clock()).atZone(ZoneId.systemDefault()))
        HomeUiState(
            userName = p.name,
            todayMillis = repo.clock(),
            pendingCaptures = d.inbox.count { it.status == com.personal.app.data.model.CaptureStatus.PENDING },
            accounts = d.accounts.sortedBy { it.createdAt },
            totalMinor = FinanceCalculator.totalBalance(d.accounts),
            monthOverMonthPercent = FinanceCalculator.monthOverMonthPercent(d.accounts, d.transactions, month),
            expenseShares = FinanceCalculator.expenseShares(d.transactions, month),
            recent = FinanceCalculator.recent(d.transactions, 5),
            hasData = d.accounts.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun togglePrivacy() = viewModelScope.launch { prefs.update { it.copy(privacyMode = !it.privacyMode) } }
}

// ---- Accounts ----

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val connections: List<BankConnection> = emptyList(),
    val totalMinor: Long = 0,
    val syncState: SyncState = SyncState.Idle,
)

class AccountsViewModel(private val repo: FinanceRepository) : ViewModel() {
    val state: StateFlow<AccountsUiState> = combine(repo.data, repo.syncState) { d, sync ->
        AccountsUiState(
            accounts = d.accounts.sortedBy { it.createdAt },
            connections = d.connections,
            totalMinor = FinanceCalculator.totalBalance(d.accounts),
            syncState = sync,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState())

    fun syncAll() = viewModelScope.launch { repo.sync() }
    fun unlink(connectionId: String) = viewModelScope.launch { repo.unlinkBank(connectionId) }
    fun deleteAccount(id: String) = viewModelScope.launch { repo.deleteAccount(id) }
}

// ---- Manual entry ----

data class AddTransactionState(
    val isExpense: Boolean = true,
    val amountText: String = "",
    val category: Category = Category.FOOD,
    val accountId: String? = null,
    val description: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val accounts: List<Account> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null,
) {
    val amountMinor: Long? get() = Money.parseToMinor(amountText)?.let { kotlin.math.abs(it) }
    val canSave: Boolean get() = (amountMinor ?: 0L) > 0L && accountId != null && description.isNotBlank()
    val categories: List<Category> get() = Category.entries.filter { it.isIncome == !isExpense }
}

class AddTransactionViewModel(private val repo: FinanceRepository) : ViewModel() {
    private val _state = MutableStateFlow(AddTransactionState(timestamp = repo.clock()))
    val state: StateFlow<AddTransactionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.accounts.collect { accounts ->
                _state.value = _state.value.copy(
                    accounts = accounts,
                    accountId = _state.value.accountId ?: accounts.firstOrNull()?.id,
                )
            }
        }
    }

    fun setExpense(expense: Boolean) = _state.value.let { s ->
        val first = Category.entries.first { it.isIncome == !expense }
        _state.value = s.copy(isExpense = expense, category = if (s.category.isIncome == !expense) s.category else first)
    }
    fun setAmount(text: String) { _state.value = _state.value.copy(amountText = text, error = null) }
    fun setCategory(c: Category) { _state.value = _state.value.copy(category = c) }
    fun setAccount(id: String) { _state.value = _state.value.copy(accountId = id) }
    fun setDescription(t: String) { _state.value = _state.value.copy(description = t) }
    fun setNote(t: String) { _state.value = _state.value.copy(note = t) }
    fun setTimestamp(t: Long) { _state.value = _state.value.copy(timestamp = t) }

    fun save() {
        val s = _state.value
        val amount = s.amountMinor ?: return
        val account = s.accountId ?: return
        viewModelScope.launch {
            runCatching {
                repo.addManualTransaction(
                    accountId = account,
                    amountMinor = if (s.isExpense) -amount else amount,
                    category = s.category,
                    description = s.description,
                    timestamp = s.timestamp,
                    note = s.note,
                )
            }.onSuccess { _state.value = s.copy(saved = true) }
                .onFailure { _state.value = s.copy(error = it.message) }
        }
    }
}

// ---- Manual account ----

data class AddAccountState(
    val name: String = "",
    val type: AccountType = AccountType.CHECKING,
    val balanceText: String = "0",
    val saved: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && Money.parseToMinor(balanceText) != null
}

class AddAccountViewModel(private val repo: FinanceRepository) : ViewModel() {
    private val _state = MutableStateFlow(AddAccountState())
    val state: StateFlow<AddAccountState> = _state.asStateFlow()
    fun setName(t: String) { _state.value = _state.value.copy(name = t) }
    fun setType(t: AccountType) { _state.value = _state.value.copy(type = t) }
    fun setBalance(t: String) { _state.value = _state.value.copy(balanceText = t) }
    fun save() {
        val s = _state.value
        val balance = Money.parseToMinor(s.balanceText) ?: return
        viewModelScope.launch {
            repo.addManualAccount(s.name, s.type, balance)
            _state.value = s.copy(saved = true)
        }
    }
}

// ---- Link a bank ----

sealed interface LinkStep {
    data object Choose : LinkStep
    data class Working(val institution: Institution, val message: Int) : LinkStep
    data class Done(val institution: Institution, val accounts: Int) : LinkStep
    data class Failed(val message: String) : LinkStep
}

data class LinkBankState(
    val providerName: String = "",
    val institutions: List<Institution> = emptyList(),
    val loading: Boolean = true,
    val step: LinkStep = LinkStep.Choose,
)

class LinkBankViewModel(private val repo: FinanceRepository, private val providerId: String = "mock") : ViewModel() {
    private val _state = MutableStateFlow(LinkBankState(providerName = repo.provider(providerId).displayName))
    val state: StateFlow<LinkBankState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val list = runCatching { repo.provider(providerId).institutions() }.getOrDefault(emptyList())
            _state.value = _state.value.copy(institutions = list, loading = false)
        }
    }

    fun link(institution: Institution) {
        viewModelScope.launch {
            _state.value = _state.value.copy(step = LinkStep.Working(institution, com.personal.app.R.string.link_step_consent))
            runCatching {
                repo.linkBank(providerId, institution.id)
            }.onSuccess { conn ->
                val count = repo.data.value.accounts.count { it.connectionId == conn.id }
                _state.value = _state.value.copy(step = LinkStep.Done(institution, count))
            }.onFailure {
                _state.value = _state.value.copy(step = LinkStep.Failed(it.message ?: "Error"))
            }
        }
    }

    fun reset() { _state.value = _state.value.copy(step = LinkStep.Choose) }
}


// ---- Settings ----

class SettingsViewModel(private val prefs: PreferencesRepository, private val repo: FinanceRepository) : ViewModel() {
    val state: StateFlow<UserPreferences> = prefs.prefs
    fun setCurrency(code: String) = viewModelScope.launch { prefs.setCurrency(code) }
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setPrivacy(on: Boolean) = viewModelScope.launch { prefs.setPrivacyMode(on) }
    fun setCaptureEnabled(on: Boolean) = viewModelScope.launch { prefs.setCaptureEnabled(on) }
    fun wipeAll() = viewModelScope.launch { repo.wipeAll() }
}

// ---- Profile ----

data class ProfileUiState(
    val name: String = "",
    val accounts: Int = 0,
    val transactions: Int = 0,
    val connections: List<BankConnection> = emptyList(),
    val since: Long? = null,
    val privacyMode: Boolean = false,
)

class ProfileViewModel(private val prefs: PreferencesRepository, private val repo: FinanceRepository) : ViewModel() {
    val state: StateFlow<ProfileUiState> = combine(prefs.prefs, repo.data) { p, d ->
        ProfileUiState(
            name = p.name,
            accounts = d.accounts.size,
            transactions = d.transactions.size,
            connections = d.connections,
            since = d.accounts.minOfOrNull { it.createdAt },
            privacyMode = p.privacyMode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun setName(name: String) = viewModelScope.launch { prefs.setName(name) }
    fun setPrivacy(on: Boolean) = viewModelScope.launch { prefs.setPrivacyMode(on) }
    fun unlink(connectionId: String) = viewModelScope.launch { repo.unlinkBank(connectionId) }
    fun wipeAll() = viewModelScope.launch { repo.wipeAll() }
}

// ---- Transactions list (all, or one account) ----

data class TransactionsUiState(
    val title: String? = null,
    val today: java.time.LocalDate = java.time.LocalDate.MIN,
    val days: List<Pair<java.time.LocalDate, List<Transaction>>> = emptyList(),
    val accountsById: Map<String, Account> = emptyMap(),
)

class TransactionsViewModel(private val repo: FinanceRepository, private val accountId: String?) : ViewModel() {
    val state: StateFlow<TransactionsUiState> = repo.data.map { d ->
        val scoped = if (accountId == null) d.transactions else d.transactions.filter { it.accountId == accountId }
        TransactionsUiState(
            title = accountId?.let { id -> d.accounts.firstOrNull { it.id == id }?.name },
            today = Instant.ofEpochMilli(repo.clock()).atZone(ZoneId.systemDefault()).toLocalDate(),
            days = FinanceCalculator.groupByDay(scoped),
            accountsById = d.accounts.associateBy { it.id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun delete(id: String) = viewModelScope.launch { repo.deleteTransaction(id) }
}

// ---- Account detail ----

data class AccountDetailUiState(
    val account: Account? = null,
    val history: List<Pair<java.time.LocalDate, Long>> = emptyList(),
    val monthIncomeMinor: Long = 0,
    val monthExpensesMinor: Long = 0,
    val recent: List<Transaction> = emptyList(),
    val transactionCount: Int = 0,
    val deleted: Boolean = false,
)

class AccountDetailViewModel(private val repo: FinanceRepository, private val accountId: String) : ViewModel() {
    private val deleted = MutableStateFlow(false)
    val state: StateFlow<AccountDetailUiState> = combine(repo.data, deleted) { d, gone ->
        val account = d.accounts.firstOrNull { it.id == accountId }
        val txs = d.transactions.filter { it.accountId == accountId }
        val month = YearMonth.from(Instant.ofEpochMilli(repo.clock()).atZone(ZoneId.systemDefault()))
        val totals = FinanceCalculator.monthTotals(txs, month)
        AccountDetailUiState(
            account = account,
            history = if (account != null) FinanceCalculator.balanceHistory(d.accounts, d.transactions, repo.clock(), 30, accountId = accountId) else emptyList(),
            monthIncomeMinor = totals.incomeMinor,
            monthExpensesMinor = totals.expensesMinor,
            recent = FinanceCalculator.recent(txs, 6),
            transactionCount = txs.size,
            deleted = gone || (account == null),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountDetailUiState())

    fun delete() = viewModelScope.launch { repo.deleteAccount(accountId); deleted.value = true }
    fun unlink(connectionId: String) = viewModelScope.launch { repo.unlinkBank(connectionId); deleted.value = true }
}


// ---- Total balance / reports ----

enum class ReportPeriod { WEEK, MONTH }

data class BalanceUiState(
    val totalMinor: Long = 0,
    val assetsMinor: Long = 0,
    val liabilitiesMinor: Long = 0,
    val monthOverMonthPercent: Double? = null,
    val history: List<Pair<java.time.LocalDate, Long>> = emptyList(),
    val period: ReportPeriod = ReportPeriod.MONTH,
    val week: com.personal.app.domain.Reports.PeriodSummary? = null,
    val month: com.personal.app.domain.Reports.PeriodSummary? = null,
    val series: List<com.personal.app.domain.Reports.MonthPoint> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val currency: String = "EUR",
    val exporting: Boolean = false,
    val exportError: String? = null,
) {
    val summary: com.personal.app.domain.Reports.PeriodSummary? get() = if (period == ReportPeriod.WEEK) week else month
}

/** A file ready to share, with its mime type. */
data class ShareRequest(val file: java.io.File, val mime: String)

class BalanceViewModel(
    private val repo: FinanceRepository,
    private val prefs: PreferencesRepository,
    private val exporter: com.personal.app.data.export.ExportManager?,
) : ViewModel() {
    private val period = MutableStateFlow(ReportPeriod.MONTH)
    private val exporting = MutableStateFlow(false)
    private val exportError = MutableStateFlow<String?>(null)
    private val _share = kotlinx.coroutines.flow.MutableSharedFlow<ShareRequest>(extraBufferCapacity = 1)
    val share: kotlinx.coroutines.flow.SharedFlow<ShareRequest> = _share

    val state: StateFlow<BalanceUiState> = combine(repo.data, prefs.prefs, period, exporting, exportError) { d, p, per, busy, err ->
        val now = repo.clock()
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val ym = YearMonth.from(today)
        BalanceUiState(
            totalMinor = FinanceCalculator.totalBalance(d.accounts),
            assetsMinor = FinanceCalculator.assets(d.accounts),
            liabilitiesMinor = d.accounts.filter { it.type == AccountType.CREDIT }.sumOf { it.balanceMinor },
            monthOverMonthPercent = FinanceCalculator.monthOverMonthPercent(d.accounts, d.transactions, ym, zone),
            history = FinanceCalculator.balanceHistory(d.accounts, d.transactions, now, 30, zone),
            period = per,
            week = com.personal.app.domain.Reports.week(d.transactions, today, zone),
            month = com.personal.app.domain.Reports.month(d.transactions, ym, zone),
            series = com.personal.app.domain.Reports.monthlySeries(d.transactions, ym, 6, zone),
            accounts = d.accounts.sortedByDescending { it.balanceMinor },
            currency = p.currency,
            exporting = busy,
            exportError = err,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BalanceUiState())

    fun setPeriod(p: ReportPeriod) { period.value = p }

    fun exportCsv() = export { it.exportCsv(repo.data.value) to "text/csv" }
    fun exportPdf() = export { it.exportPdf(repo.data.value, prefs.prefs.value.currency, prefs.prefs.value.name) to "application/pdf" }

    private fun export(block: (com.personal.app.data.export.ExportManager) -> Pair<java.io.File, String>) {
        val ex = exporter ?: run { exportError.value = "Export unavailable"; return }
        viewModelScope.launch {
            exporting.value = true; exportError.value = null
            runCatching { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { block(ex) } }
                .onSuccess { (file, mime) -> _share.tryEmit(ShareRequest(file, mime)) }
                .onFailure { exportError.value = it.message ?: it.javaClass.simpleName }
            exporting.value = false
        }
    }
}

// ---- Bank notification inbox ----

data class InboxUiState(
    val items: List<com.personal.app.data.model.CapturedTransaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
)

class InboxViewModel(private val repo: FinanceRepository) : ViewModel() {
    val state: StateFlow<InboxUiState> = combine(repo.pendingCaptures, repo.accounts) { items, accounts ->
        InboxUiState(items = items, accounts = accounts)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InboxUiState())

    fun accept(id: String, accountId: String, amountMinor: Long, category: Category, description: String) =
        viewModelScope.launch { runCatching { repo.acceptCaptured(id, accountId, amountMinor, category, description) } }

    fun dismiss(id: String) = viewModelScope.launch { repo.dismissCaptured(id) }
}

// ---- Capture settings ----

data class CaptureSettingsState(
    val enabled: Boolean = true,
    val pending: Int = 0,
    val sampleText: String = "",
    val sampleParsed: com.personal.app.data.capture.ParsedNotification? = null,
    val sampleAdded: Boolean = false,
)

class CaptureSettingsViewModel(private val prefs: PreferencesRepository, private val repo: FinanceRepository) : ViewModel() {
    private val sample = MutableStateFlow("")
    private val added = MutableStateFlow(false)
    val state: StateFlow<CaptureSettingsState> = combine(prefs.prefs, repo.pendingCaptures, sample, added) { p, pending, text, wasAdded ->
        CaptureSettingsState(
            enabled = p.captureEnabled,
            pending = pending.size,
            sampleText = text,
            sampleParsed = if (text.isBlank()) null else com.personal.app.data.capture.BankNotificationParser.parse("BBVA", text),
            sampleAdded = wasAdded,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaptureSettingsState())

    fun setEnabled(on: Boolean) = viewModelScope.launch { prefs.setCaptureEnabled(on) }
    fun setSample(text: String) { sample.value = text; added.value = false }

    /** Pushes the pasted text through the same path a real notification takes. */
    fun addSampleToInbox() = viewModelScope.launch {
        val text = sample.value.trim()
        if (text.isEmpty()) return@launch
        repo.addCaptured(com.personal.app.data.capture.BankNotificationListener.build("com.bbva.bbvacontigo", "BBVA", text, repo.clock()))
        added.value = true
    }
}
