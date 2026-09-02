package com.badminton.scorecard.feature.match_summary.presentation.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.TeamAColor
import com.badminton.scorecard.core.designsystem.theme.TeamBColor
import com.badminton.scorecard.feature.match_summary.presentation.MatchSummaryViewModel.ScorePoint

@Composable
fun ScoreProgressionChart(
    data: List<ScorePoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxScore = maxOf(
        data.maxOfOrNull { it.teamAScore } ?: 0,
        data.maxOfOrNull { it.teamBScore } ?: 0,
        1
    )
    val maxRally = maxOf(data.maxOfOrNull { it.rallyNumber } ?: 1, 1)

    Canvas(modifier = modifier
        .height(200.dp)
        .fillMaxWidth()
    ) {
        val width = size.width
        val height = size.height
        
        val padding = 40f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        
        // Draw grid
        val xStep = chartWidth / maxRally
        val yStep = chartHeight / maxScore
        
        for (i in 0..maxScore step 5) {
            val y = padding + chartHeight - (i * yStep)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f
            )
        }

        // Draw Line for Team A
        val pathA = Path()
        data.forEachIndexed { index, point ->
            val x = padding + (point.rallyNumber * xStep)
            val y = padding + chartHeight - (point.teamAScore * yStep)
            if (index == 0) {
                pathA.moveTo(x, y)
            } else {
                pathA.lineTo(x, y)
            }
        }
        
        drawPath(
            path = pathA,
            color = TeamAColor,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw Line for Team B
        val pathB = Path()
        data.forEachIndexed { index, point ->
            val x = padding + (point.rallyNumber * xStep)
            val y = padding + chartHeight - (point.teamBScore * yStep)
            if (index == 0) {
                pathB.moveTo(x, y)
            } else {
                pathB.lineTo(x, y)
            }
        }

        drawPath(
            path = pathB,
            color = TeamBColor,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScoreProgressionChart() {
    ScoreProgressionChart(
        data = listOf(
            ScorePoint(0, 0, 0),
            ScorePoint(1, 1, 0),
            ScorePoint(2, 1, 1),
            ScorePoint(3, 2, 1),
            ScorePoint(4, 2, 2),
            ScorePoint(5, 3, 2),
            ScorePoint(6, 4, 2),
            ScorePoint(7, 5, 2)
        )
    )
}
