package dev.watchbox.tv

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import dev.watchbox.WatchBoxFactory
import dev.watchbox.data.local.createLibraryStore
import dev.watchbox.ui.WatchBoxApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val preferences = getSharedPreferences("watchbox_library", Context.MODE_PRIVATE)
        val libraryStore = createLibraryStore(preferences)
        val viewModel = WatchBoxFactory.createViewModel(libraryStore, scope)

        setContent {
            WatchBoxApp(viewModel = viewModel, modifier = Modifier)
        }
    }
}
