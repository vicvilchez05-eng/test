package com.personal.app.data.store

import com.personal.app.data.model.FinanceData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * The persistence boundary. The whole dataset lives in memory as a [StateFlow]; every write is a
 * pure transform applied under a mutex and then persisted. Small enough for a personal ledger
 * (years of transactions are a few MB) and trivially swappable for SQL later (HANDOFF D-025).
 */
interface FinanceStore {
    val data: StateFlow<FinanceData>
    suspend fun update(transform: (FinanceData) -> FinanceData): FinanceData
}

/** Tests and previews. */
class InMemoryFinanceStore(initial: FinanceData = FinanceData()) : FinanceStore {
    private val state = MutableStateFlow(initial)
    private val mutex = Mutex()
    override val data: StateFlow<FinanceData> = state.asStateFlow()
    override suspend fun update(transform: (FinanceData) -> FinanceData): FinanceData = mutex.withLock {
        state.value = transform(state.value)
        state.value
    }
}

/**
 * One JSON file in the app's private storage, written atomically (temp file + rename) so a crash
 * mid-write never corrupts the ledger. A corrupt or unreadable file falls back to an empty dataset
 * and is kept aside as `.corrupt` for inspection.
 */
class JsonFileFinanceStore(
    private val file: File,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = false },
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : FinanceStore {
    private val state = MutableStateFlow(load())
    private val mutex = Mutex()
    override val data: StateFlow<FinanceData> = state.asStateFlow()

    private fun load(): FinanceData {
        if (!file.exists()) return FinanceData()
        return runCatching { json.decodeFromString(FinanceData.serializer(), file.readText()) }
            .getOrElse {
                file.renameTo(File(file.parentFile, file.name + ".corrupt"))
                FinanceData()
            }
    }

    override suspend fun update(transform: (FinanceData) -> FinanceData): FinanceData = mutex.withLock {
        val next = transform(state.value)
        withContext(io) {
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(json.encodeToString(FinanceData.serializer(), next))
            if (!tmp.renameTo(file)) {
                file.delete()
                tmp.renameTo(file)
            }
        }
        state.value = next
        next
    }
}
