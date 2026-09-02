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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme
import com.badminton.scorecard.core.designsystem.theme.CourtGreen

@Composable
fun ServeStatsChart(
    serveHoldRate: Float,
    returnWinRate: Float,
    partnerServeRate: Float,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val serveColor = CourtGreen
    val returnColor = Color(0xFFE57373)
    val partnerColor = Color(0xFF64B5F6)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface

    val data = listOf(
        Triple("Hold", serveHoldRate, serveColor),
        Triple("Return", returnWinRate, returnColor),
        Triple("Partner", partnerServeRate, partnerColor)
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height
        
        val barWidth = 40.dp.toPx()
        val maxBarHeight = height - 40.dp.toPx()
        
        val spacing = (width - (barWidth * 3)) / 4f

        data.forEachIndexed { index, (label, rate, color) ->
            val xOffset = spacing + index * (barWidth + spacing)
            
            // Draw track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(xOffset, 20.dp.toPx()),
                size = Size(barWidth, maxBarHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            
            // Draw filled bar
            val fillHeight = maxBarHeight * (rate / 100f)
            if (fillHeight > 0) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(xOffset, 20.dp.toPx() + maxBarHeight - fillHeight),
                    size = Size(barWidth, fillHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
            
            // Draw label
            val textLayoutResult = textMeasurer.measure(
                text = label,
                style = TextStyle(color = textColor, fontSize = 12.sp, textAlign = TextAlign.Center)
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(xOffset + (barWidth - textLayoutResult.size.width) / 2f, height - 16.dp.toPx())
            )
            
            // Draw percentage
            val pctLayoutResult = textMeasurer.measure(
                text = "${rate.toInt()}%",
                style = TextStyle(color = textColor, fontSize = 10.sp, textAlign = TextAlign.Center)
            )
            drawText(
                textLayoutResult = pctLayoutResult,
                topLeft = Offset(xOffset + (barWidth - pctLayoutResult.size.width) / 2f, 4.dp.toPx())
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServeStatsChartPreview() {
    BadmintonScorecardTheme {
        ServeStatsChart(
            serveHoldRate = 65f,
            returnWinRate = 45f,
            partnerServeRate = 55f
        )
    }
}
