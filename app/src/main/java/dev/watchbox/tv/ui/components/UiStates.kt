package dev.watchbox.tv.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.watchbox.tv.R
import dev.watchbox.tv.ui.theme.Grey500
import dev.watchbox.tv.ui.theme.Grey700
import dev.watchbox.tv.ui.theme.Navy800

@Composable
fun LoadingShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )
    val brush = Brush.linearGradient(
        colors = listOf(Navy800, Grey700, Navy800),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f),
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
    ) {
        // Hero placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush),
        )
        Spacer(modifier = Modifier.height(32.dp))
        // Shelf placeholders
        repeat(2) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(124.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(brush),
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Grey500,
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onRetry() }) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.offline_message),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onError,
        )
    }
}
