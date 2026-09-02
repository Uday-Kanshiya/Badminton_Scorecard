package com.badminton.scorecard.core.designsystem.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme

enum class TimePeriod {
    DAILY, WEEKLY, MONTHLY, ALL_TIME;
    
    fun getDisplayName(): String {
        return when (this) {
            DAILY -> "Daily"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
            ALL_TIME -> "All Time"
        }
    }
}

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(text = period.getDisplayName()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimePeriodSelectorPreview() {
    BadmintonScorecardTheme {
        TimePeriodSelector(
            selectedPeriod = TimePeriod.WEEKLY,
            onPeriodSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
