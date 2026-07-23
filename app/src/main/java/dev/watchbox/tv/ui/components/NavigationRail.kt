package dev.watchbox.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.watchbox.tv.R
import dev.watchbox.tv.ui.theme.Coral500
import dev.watchbox.tv.ui.theme.Navy900

enum class NavDestination(val route: String, val labelRes: Int, val icon: String) {
    HOME("home", R.string.nav_home, "🏠"),
    SEARCH("search", R.string.nav_search, "🔍"),
    LIBRARY("library", R.string.nav_library, "📚"),
}

@Composable
fun WatchBoxNavigationRail(
    selectedDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(Navy900)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavDestination.entries.forEach { destination ->
            val isSelected = destination == selectedDestination
            Button(
                onClick = { onDestinationSelected(destination) },
                shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = destination.icon,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(destination.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Coral500 else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
