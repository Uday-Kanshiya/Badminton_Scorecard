package com.badminton.scorecard.feature.statistics.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme
import com.badminton.scorecard.core.designsystem.theme.CourtGreen

@Composable
fun WinRateTrendChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val lineColor = CourtGreen

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        
        val padding = 32.dp.toPx()
        val graphWidth = width - padding * 2
        val graphHeight = height - padding * 2
        
        // Draw grid and Y-axis labels
        val ySteps = 4
        for (i in 0..ySteps) {
            val y = padding + (graphHeight * i / ySteps)
            val value = 100 - (100 * i / ySteps)
            
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
            
            drawText(
                textMeasurer = textMeasurer,
                text = "$value%",
                style = TextStyle(color = labelColor, fontSize = 10.sp),
                topLeft = Offset(0f, y - 5.dp.toPx())
            )
        }
        
        if (data.size > 1) {
            val xStep = graphWidth / (data.size - 1)
            val path = Path()
            val gradientPath = Path()
            
            data.forEachIndexed { index, pair ->
                val x = padding + index * xStep
                val y = padding + graphHeight * (1f - (pair.second / 100f))
                
                if (index == 0) {
                    path.moveTo(x, y)
                    gradientPath.moveTo(x, padding + graphHeight)
                    gradientPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    gradientPath.lineTo(x, y)
                }
                
                // Draw X-axis labels (draw max 5 labels to avoid crowding)
                val step = if (data.size > 5) data.size / 5 else 1
                if (index % step == 0 || index == data.size - 1) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = pair.first,
                        style = TextStyle(color = labelColor, fontSize = 10.sp),
                        topLeft = Offset(x - 10.dp.toPx(), height - padding + 4.dp.toPx())
                    )
                }
            }
            
            gradientPath.lineTo(padding + graphWidth, padding + graphHeight)
            gradientPath.close()
            
            drawPath(
                path = gradientPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = padding,
                    endY = padding + graphHeight
                )
            )
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        } else if (data.size == 1) {
            // Draw a single point
            val x = padding + graphWidth / 2f
            val y = padding + graphHeight * (1f - (data[0].second / 100f))
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = data[0].first,
                style = TextStyle(color = labelColor, fontSize = 10.sp),
                topLeft = Offset(x - 10.dp.toPx(), height - padding + 4.dp.toPx())
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WinRateTrendChartPreview() {
    BadmintonScorecardTheme {
        WinRateTrendChart(
            data = listOf(
                "Jan" to 40f,
                "Feb" to 55f,
                "Mar" to 50f,
                "Apr" to 70f,
                "May" to 85f
            )
        )
    }
}
