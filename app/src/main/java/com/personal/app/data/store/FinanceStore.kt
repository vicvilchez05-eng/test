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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * The persistence boundary. A whole document lives in memory as a [StateFlow]; every write is a
 * pure transform applied under a mutex and then persisted. Small enough for a personal ledger
 * (years of transactions are a few MB) and trivially swappable for SQL later (HANDOFF D-025).
 */
interface Store<T> {
    val data: StateFlow<T>
    suspend fun update(transform: (T) -> T): T
}

typealias FinanceStore = Store<FinanceData>

/** Tests and previews. */
class InMemoryStore<T>(initial: T) : Store<T> {
    private val state = MutableStateFlow(initial)
    private val mutex = Mutex()
    override val data: StateFlow<T> = state.asStateFlow()
    override suspend fun update(transform: (T) -> T): T = mutex.withLock {
        state.value = transform(state.value)
        state.value
    }
}

@Suppress("FunctionName")
fun InMemoryFinanceStore(initial: FinanceData = FinanceData()): FinanceStore = InMemoryStore(initial)

/**
 * One JSON file in the app's private storage, written atomically (temp file + rename) so a crash
 * mid-write never corrupts the document. A corrupt or unreadable file falls back to [default]
 * and is kept aside as `.corrupt` for inspection.
 */
class JsonFileStore<T>(
    private val file: File,
    private val serializer: KSerializer<T>,
    private val default: T,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = false },
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : Store<T> {
    private val state = MutableStateFlow(load())
    private val mutex = Mutex()
    override val data: StateFlow<T> = state.asStateFlow()

    private fun load(): T {
        if (!file.exists()) return default
        return runCatching { json.decodeFromString(serializer, file.readText()) }
            .getOrElse {
                file.renameTo(File(file.parentFile, file.name + ".corrupt"))
                default
            }
    }

    override suspend fun update(transform: (T) -> T): T = mutex.withLock {
        val next = transform(state.value)
        withContext(io) {
            file.parentFile?.mkdirs()
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(json.encodeToString(serializer, next))
            if (!tmp.renameTo(file)) {
                file.delete()
                tmp.renameTo(file)
            }
        }
        state.value = next
        next
    }
}

@Suppress("FunctionName")
fun JsonFileFinanceStore(file: File): FinanceStore = JsonFileStore(file, FinanceData.serializer(), FinanceData())
