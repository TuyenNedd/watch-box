package dev.watchbox.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import dev.watchbox.WatchBoxFactory
import dev.watchbox.data.local.createBrowserLibraryStore
import dev.watchbox.ui.WatchBoxApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val libraryStore = createBrowserLibraryStore()
    val viewModel = WatchBoxFactory.createViewModel(libraryStore, scope)

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        WatchBoxApp(viewModel = viewModel)
    }
}
