package dev.watchbox.tv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.watchbox.tv.R
import dev.watchbox.tv.ui.theme.Grey400
import dev.watchbox.tv.ui.theme.Grey500
import dev.watchbox.tv.ui.theme.Navy800
import dev.watchbox.tv.ui.theme.White

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = White,
            fontSize = 18.sp,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy800, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Grey500,
                    )
                }
                innerTextField()
            }
        },
        modifier = modifier,
    )
}
