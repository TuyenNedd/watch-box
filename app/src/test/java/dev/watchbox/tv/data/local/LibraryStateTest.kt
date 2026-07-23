package dev.watchbox.tv.data.local

import android.content.SharedPreferences
import dev.watchbox.tv.core.model.PlaybackProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryStateTest {
    @Test
    fun `setting favorite is idempotent and updates state synchronously`() {
        val preferences = FakeSharedPreferences()
        val store = PreferencesLibraryStore(preferences)

        assertTrue(store.setFavorite("movie", true))
        assertTrue(store.setFavorite("movie", true))
        assertEquals(setOf("movie"), store.favoriteIds.value)
        assertEquals(setOf("movie"), preferences.getStringSet("favorites", mutableSetOf()))

        assertFalse(store.setFavorite("movie", false))
        assertFalse(store.setFavorite("movie", false))
        assertTrue(store.favoriteIds.value.isEmpty())
    }

    @Test
    fun `completed progress is removed while useful progress is retained`() {
        val store = PreferencesLibraryStore(FakeSharedPreferences())

        store.saveProgress("movie", PlaybackProgress(30_000), durationMs = 180_000)
        assertEquals(30_000L, store.progress.value["movie"]?.positionMs)

        store.saveProgress("movie", PlaybackProgress(171_000), durationMs = 180_000)
        assertNull(store.progress.value["movie"])
    }

    @Test
    fun `favorites and progress survive reconstruction`() {
        val preferences = FakeSharedPreferences()
        PreferencesLibraryStore(preferences).apply {
            setFavorite("movie", true)
            saveProgress("movie", PlaybackProgress(30_000, 1234), durationMs = 180_000)
        }

        val restored = PreferencesLibraryStore(preferences)

        assertEquals(setOf("movie"), restored.favoriteIds.value)
        assertEquals(PlaybackProgress(30_000, 1234), restored.progress.value["movie"])
    }

    @Test
    fun `one corrupt progress entry does not discard valid siblings`() {
        val preferences = FakeSharedPreferences(
            mutableMapOf(
                "progress" to "{\"good\":{\"positionMs\":30000,\"updatedAtEpochMs\":2},\"bad\":{\"positionMs\":\"oops\"}}",
            ),
        )

        val store = PreferencesLibraryStore(preferences)

        assertEquals(PlaybackProgress(30_000, 2), store.progress.value["good"])
        assertNull(store.progress.value["bad"])
    }

    @Test
    fun `invalid negative progress is rejected on write and load`() {
        val preferences = FakeSharedPreferences(
            mutableMapOf("progress" to "{\"bad\":{\"positionMs\":-1,\"updatedAtEpochMs\":2}}"),
        )
        val store = PreferencesLibraryStore(preferences)

        store.saveProgress("also-bad", PlaybackProgress(-10), durationMs = 100_000)

        assertTrue(store.progress.value.isEmpty())
    }
}

private class FakeSharedPreferences(
    private val values: MutableMap<String, Any?> = mutableMapOf(),
) : SharedPreferences {
    override fun getAll(): MutableMap<String, *> = values.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = values[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        ((values[key] as? Set<String>) ?: defValues)?.toMutableSet()
    override fun getInt(key: String?, defValue: Int): Int = values[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = values[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = values[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = values.containsKey(key)
    override fun edit(): SharedPreferences.Editor = Editor(values)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    private class Editor(private val values: MutableMap<String, Any?>) : SharedPreferences.Editor {
        private val updates = mutableMapOf<String, Any?>()
        private var clear = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor = apply { updates[key] = value }
        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = apply {
            updates[key] = values?.toSet()
        }
        override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply { updates[key] = value }
        override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply { updates[key] = value }
        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply { updates[key] = value }
        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply { updates[key] = value }
        override fun remove(key: String): SharedPreferences.Editor = apply { updates[key] = null }
        override fun clear(): SharedPreferences.Editor = apply { clear = true }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() {
            if (clear) values.clear()
            updates.forEach { (key, value) -> if (value == null) values.remove(key) else values[key] = value }
        }
    }
}
