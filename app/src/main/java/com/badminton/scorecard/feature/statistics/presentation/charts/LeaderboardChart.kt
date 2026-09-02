package com.badminton.scorecard.feature.statistics.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme

@Composable
fun LeaderboardChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val barHeight = 24.dp
    val spacing = 16.dp
    
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = Color(0xFFFFD700)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val totalHeight = (barHeight + spacing) * data.size
    val calcHeight = if (totalHeight > 0.dp) totalHeight else 0.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(calcHeight)
    ) {
        val width = size.width
        val nameWidth = width * 0.3f
        val chartWidth = width - nameWidth - 40.dp.toPx()

        data.forEachIndexed { index, (name, winRate) ->
            val yOffset = index * (barHeight.toPx() + spacing.toPx())
            
            // Draw Name
            drawText(
                textMeasurer = textMeasurer,
                text = name,
                style = TextStyle(color = textColor, fontSize = 14.sp),
                topLeft = Offset(0f, yOffset + (barHeight.toPx() - 14.sp.toPx()) / 2f)
            )

            // Draw Track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(nameWidth, yOffset),
                size = Size(chartWidth, barHeight.toPx()),
                cornerRadius = CornerRadius(barHeight.toPx() / 2f)
            )

            // Draw Bar
            val barColor = if (index == 0) goldColor else primaryColor
            val barWidth = chartWidth * (winRate / 100f)
            if (barWidth > 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(nameWidth, yOffset),
                    size = Size(barWidth, barHeight.toPx()),
                    cornerRadius = CornerRadius(barHeight.toPx() / 2f)
                )
            }

            // Draw Percentage
            drawText(
                textMeasurer = textMeasurer,
                text = "${winRate.toInt()}%",
                style = TextStyle(color = textColor, fontSize = 14.sp),
                topLeft = Offset(nameWidth + chartWidth + 8.dp.toPx(), yOffset + (barHeight.toPx() - 14.sp.toPx()) / 2f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardChartPreview() {
    BadmintonScorecardTheme {
        LeaderboardChart(
            data = listOf(
                "John Doe" to 85f,
                "Alice S." to 72f,
                "Bob B." to 45f,
                "Maya" to 30f
            )
        )
    }
}
