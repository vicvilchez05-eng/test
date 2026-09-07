package com.personal.app.data

import com.personal.app.data.prefs.ThemeMode
import com.personal.app.data.prefs.UserPreferences
import com.personal.app.data.store.JsonFileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class JsonFileStoreTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun store(file: File) = JsonFileStore(file, UserPreferences.serializer(), UserPreferences(), io = Dispatchers.Unconfined)

    @Test
    fun writes_survive_a_reload() = runTest {
        val file = File(tmp.root, "settings.json")
        store(file).update { it.copy(name = "Vic", themeMode = ThemeMode.DARK, currency = "USD") }
        val reloaded = store(file).data.value
        assertEquals("Vic", reloaded.name)
        assertEquals(ThemeMode.DARK, reloaded.themeMode)
        assertEquals("USD", reloaded.currency)
        assertTrue(!File(tmp.root, "settings.json.tmp").exists())
    }

    @Test
    fun corrupt_file_falls_back_to_default_and_is_kept_aside() = runTest {
        val file = File(tmp.root, "settings.json").apply { writeText("{ this is not json") }
        val s = store(file)
        assertEquals(UserPreferences(), s.data.value)
        assertTrue(File(tmp.root, "settings.json.corrupt").exists())
    }

    @Test
    fun unknown_keys_are_ignored_for_forward_compatibility() = runTest {
        val file = File(tmp.root, "settings.json").apply { writeText("""{"name":"Vic","futureField":42}""") }
        assertEquals("Vic", store(file).data.value.name)
    }
}
