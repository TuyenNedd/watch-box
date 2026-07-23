package dev.watchbox.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.watchbox.ui.theme.Navy800

data class BrowseMenuItem(
    val title: String,
    val key: String,
)

private val browseMenuItems = listOf(
    BrowseMenuItem("Movies", "phim-le"),
    BrowseMenuItem("Series", "phim-bo"),
    BrowseMenuItem("Animation", "hoat-hinh"),
    BrowseMenuItem("TV Shows", "tv-shows"),
    BrowseMenuItem("Genres", "genres"),
    BrowseMenuItem("Countries", "countries"),
)

@Composable
fun BrowseMenuScreen(
    onTypeClick: (type: String) -> Unit,
    onGenresClick: () -> Unit,
    onCountriesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Browse",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = browseMenuItems, key = { it.key }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        when (item.key) {
                            "genres" -> onGenresClick()
                            "countries" -> onCountriesClick()
                            else -> onTypeClick(item.key)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 24.dp),
                    )
                }
            }
        }
    }
}
