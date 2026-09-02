package com.badminton.scorecard.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme
import com.badminton.scorecard.core.designsystem.theme.CourtGreen

@Composable
fun PlayerChip(
    name: String,
    modifier: Modifier = Modifier,
    avatarUri: String? = null,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()

    Surface(
        modifier = modifier
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) CourtGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = if (isSelected) BorderStroke(1.5.dp, CourtGreen) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerChipPreview() {
    BadmintonScorecardTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            PlayerChip(name = "Uday Kiran", isSelected = false)
            Spacer(modifier = Modifier.width(8.dp))
            PlayerChip(name = "John Doe", isSelected = true, onClick = {})
        }
    }
}
