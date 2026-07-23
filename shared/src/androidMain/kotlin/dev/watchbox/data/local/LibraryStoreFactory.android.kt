package dev.watchbox.data.local

import android.content.SharedPreferences

fun createLibraryStore(preferences: SharedPreferences): LibraryStore {
    return PreferencesLibraryStore(preferences)
}
