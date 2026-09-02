package com.badminton.scorecard.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme
import com.badminton.scorecard.core.designsystem.theme.LossRed
import com.badminton.scorecard.core.designsystem.theme.WinGreen
import com.badminton.scorecard.core.designsystem.theme.statLabel
import com.badminton.scorecard.core.designsystem.theme.statValue

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trend: Float? = null,
    containerColor: androidx.compose.ui.graphics.Color? = null,
    borderColor: androidx.compose.ui.graphics.Color? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor ?: MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor ?: MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    style = statLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = statValue,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (trend != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (trend >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (trend >= 0) "Trend Up" else "Trend Down",
                        tint = if (trend >= 0) WinGreen else LossRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "${kotlin.math.abs(trend)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (trend >= 0) WinGreen else LossRed,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatCardPreview() {
    BadmintonScorecardTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            StatCard(
                label = "Total Matches",
                value = "142",
                subtitle = "This season"
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatCard(
                label = "Win Rate",
                value = "68%",
                trend = 5.2f
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatCard(
                label = "Average Points",
                value = "18.5",
                trend = -1.2f
            )
        }
    }
}
