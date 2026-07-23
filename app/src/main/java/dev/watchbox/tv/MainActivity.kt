package dev.watchbox.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.tv.material3.Surface
import dev.watchbox.tv.ui.WatchBoxApp
import dev.watchbox.tv.ui.theme.WatchBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as WatchBoxApplication
        setContent {
            WatchBoxTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WatchBoxApp(application = app)
                }
            }
        }
    }
}
