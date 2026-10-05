package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SeriesTarget
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.HumanCyan
import com.example.ui.theme.VictoryGold

@Composable
fun ScoreboardCard(
  currentRound: Int,
  humanScore: Int,
  aiScore: Int,
  drawScore: Int,
  currentStreak: Int,
  seriesTarget: SeriesTarget,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("scoreboard_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Header row: Round badge and series badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
          Text(
            text = "ROUND $currentRound",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }

        // Mode badge or streak indicator
        if (seriesTarget != SeriesTarget.CONTINUOUS) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = VictoryGold.copy(alpha = 0.2f)
          ) {
            Text(
              text = "${seriesTarget.label} (${seriesTarget.winsNeeded} to win)",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = VictoryGold
            )
          }
        } else if (currentStreak != 0) {
          val isHumanStreak = currentStreak > 0
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isHumanStreak) HumanCyan.copy(alpha = 0.2f) else GeminiViolet.copy(alpha = 0.2f)
          ) {
            Text(
              text = if (isHumanStreak) "Streak: $currentStreak 🔥" else "AI Streak: ${-currentStreak} 🤖",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = if (isHumanStreak) HumanCyan else GeminiViolet
            )
          }
        } else {
          Text(
            text = "Continuous Match",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3-column score tally: Human - Ties - Gemini AI
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Human player column
        ScoreColumn(
          playerName = "You (X)",
          score = humanScore,
          color = HumanCyan,
          icon = {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Human Player",
              tint = HumanCyan,
              modifier = Modifier.size(18.dp)
            )
          },
          testTag = "human_score_display"
        )

        // Divider
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        // Draws column
        ScoreColumn(
          playerName = "Draws",
          score = drawScore,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          icon = null,
          testTag = "draws_score_display"
        )

        // Divider
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        // Gemini AI column
        ScoreColumn(
          playerName = "Gemini (O)",
          score = aiScore,
          color = GeminiViolet,
          icon = {
            Icon(
              imageVector = Icons.Default.SmartToy,
              contentDescription = "Gemini AI",
              tint = GeminiViolet,
              modifier = Modifier.size(18.dp)
            )
          },
          testTag = "ai_score_display"
        )
      }
    }
  }
}

@Composable
private fun ScoreColumn(
  playerName: String,
  score: Int,
  color: Color,
  icon: (@Composable () -> Unit)?,
  testTag: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      icon?.invoke()
      Text(
        text = playerName,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = color
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    AnimatedContent(
      targetState = score,
      transitionSpec = {
        slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
      },
      label = "score_counter"
    ) { targetScore ->
      Text(
        text = targetScore.toString(),
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 32.sp
        ),
        color = color
      )
    }
  }
}
