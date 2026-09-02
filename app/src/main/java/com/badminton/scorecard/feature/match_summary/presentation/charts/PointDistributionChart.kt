package com.badminton.scorecard.feature.match_summary.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PointDistributionChart(
    servePoints: Int,
    returnPoints: Int,
    teamColor: Color,
    modifier: Modifier = Modifier
) {
    val total = servePoints + returnPoints
    if (total == 0) return

    val serveAngle = (servePoints.toFloat() / total) * 360f
    val returnAngle = 360f - serveAngle

    Canvas(modifier = modifier.size(100.dp)) {
        val strokeWidth = 20.dp.toPx()
        val sizeVal = size.minDimension - strokeWidth
        
        // Draw serve arc
        drawArc(
            color = teamColor,
            startAngle = -90f,
            sweepAngle = serveAngle,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(sizeVal, sizeVal),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        
        // Draw return arc
        drawArc(
            color = teamColor.copy(alpha = 0.5f),
            startAngle = -90f + serveAngle,
            sweepAngle = returnAngle,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(sizeVal, sizeVal),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPointDistributionChart() {
    PointDistributionChart(
        servePoints = 15,
        returnPoints = 6,
        teamColor = Color.Blue
    )
}
