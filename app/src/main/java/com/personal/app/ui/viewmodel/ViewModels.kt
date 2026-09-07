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
    val accounts: List<Account> = emptyList(),
    val totalMinor: Long = 0,
    val monthOverMonthPercent: Double? = null,
    val expenseShares: List<Pair<Category, Float>> = emptyList(),
    val recent: List<Transaction> = emptyList(),
    val hasData: Boolean = false,
)

class HomeViewModel(private val repo: FinanceRepository) : ViewModel() {
    val state: StateFlow<HomeUiState> = repo.data.map { d ->
        val month = YearMonth.from(Instant.ofEpochMilli(repo.clock()).atZone(ZoneId.systemDefault()))
        HomeUiState(
            accounts = d.accounts.sortedBy { it.createdAt },
            totalMinor = FinanceCalculator.totalBalance(d.accounts),
            monthOverMonthPercent = FinanceCalculator.monthOverMonthPercent(d.accounts, d.transactions, month),
            expenseShares = FinanceCalculator.expenseShares(d.transactions, month),
            recent = FinanceCalculator.recent(d.transactions, 5),
            hasData = d.accounts.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
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
