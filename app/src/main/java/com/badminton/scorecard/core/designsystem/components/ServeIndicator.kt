package com.badminton.scorecard.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme
import com.badminton.scorecard.core.designsystem.theme.ServingHighlight

@Composable
fun ServeIndicator(
    serverName: String,
    courtSide: String, // "RIGHT" or "LEFT"
    modifier: Modifier = Modifier,
    isAnimated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val scale by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ShuttlecockPulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    }

    Row(
        modifier = modifier
            .background(
                color = ServingHighlight,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏸",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.scale(scale)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$serverName serving ($courtSide court)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ServeIndicatorPreview() {
    BadmintonScorecardTheme {
        ServeIndicator(
            serverName = "Uday Kiran",
            courtSide = "RIGHT",
            isAnimated = false
        )
    }
}
