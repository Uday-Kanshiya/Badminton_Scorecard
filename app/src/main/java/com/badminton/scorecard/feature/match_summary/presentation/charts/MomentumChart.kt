package com.badminton.scorecard.feature.match_summary.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.TeamAColor
import com.badminton.scorecard.core.designsystem.theme.TeamBColor
import kotlin.math.abs

@Composable
fun MomentumChart(
    data: List<Int>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxDiff = maxOf(data.maxOfOrNull { abs(it) } ?: 1, 1)
    
    Canvas(modifier = modifier.height(150.dp).fillMaxWidth()) {
        val width = size.width
        val height = size.height
        
        val padding = 20f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        
        val centerY = height / 2f
        val barWidth = chartWidth / data.size.coerceAtLeast(1)
        val yStep = (chartHeight / 2f) / maxDiff

        // Draw center line
        drawLine(
            color = Color.Gray,
            start = Offset(padding, centerY),
            end = Offset(width - padding, centerY),
            strokeWidth = 2f
        )

        // Draw bars
        data.forEachIndexed { index, diff ->
            val barHeight = abs(diff) * yStep
            val x = padding + (index * barWidth)
            val barColor = if (diff > 0) TeamAColor else if (diff < 0) TeamBColor else Color.Transparent
            
            val y = if (diff > 0) centerY - barHeight else centerY
            
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth * 0.8f, barHeight)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMomentumChart() {
    MomentumChart(
        data = listOf(0, 1, 0, -1, -2, -1, 0, 1, 2, 3, 2, 1, 0, -1)
    )
}
