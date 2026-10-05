package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameStatus
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.HumanCyan
import com.example.ui.theme.VictoryGold
import com.example.ui.theme.VictoryGoldLight

@Composable
fun WinningVerdictCard(
  gameStatus: GameStatus,
  currentRound: Int,
  humanScore: Int,
  aiScore: Int,
  onPlayNextRound: () -> Unit,
  onResetSeries: () -> Unit,
  modifier: Modifier = Modifier
) {
  val (isWinner, winnerName, commentary, isSeriesEnd) = when (gameStatus) {
    is GameStatus.RoundOver -> {
      val isWin = gameStatus.winner == "HUMAN"
      Quad(isWin, gameStatus.winner, gameStatus.commentary, false)
    }
    is GameStatus.SeriesConcluded -> {
      val isWin = gameStatus.champion == "HUMAN"
      Quad(isWin, gameStatus.champion, gameStatus.commentary, true)
    }
    else -> Quad(false, "", "", false)
  }

  val headline = when {
    isSeriesEnd && isWinner -> "🏆 Series Champion!"
    isSeriesEnd -> "🤖 Gemini Won the Series!"
    winnerName == "HUMAN" -> "🎉 Round Victory!"
    winnerName == "GEMINI" -> "🤖 Gemini Won This Round"
    else -> "🤝 Stalemate (Draw)"
  }

  val accentColor = when {
    winnerName == "HUMAN" -> VictoryGold
    winnerName == "GEMINI" -> GeminiViolet
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  val headerIcon = when {
    winnerName == "HUMAN" -> Icons.Default.EmojiEvents
    winnerName == "GEMINI" -> Icons.Default.SmartToy
    else -> Icons.Default.Handshake
  }

  AnimatedVisibility(
    visible = gameStatus is GameStatus.RoundOver || gameStatus is GameStatus.SeriesConcluded,
    enter = fadeIn() + scaleIn(animationSpec = spring(stiffness = 300f)),
    modifier = modifier
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("winning_verdict_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = BorderStroke(
        2.dp,
        Brush.horizontalGradient(
          listOf(accentColor.copy(alpha = 0.8f), accentColor.copy(alpha = 0.3f))
        )
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icon badge
        Box(
          modifier = Modifier
            .size(56.dp)
            .background(accentColor.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = headerIcon,
            contentDescription = "Verdict Result",
            tint = accentColor,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Headline
        Text(
          text = headline,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
          ),
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle / Round standing
        Text(
          text = if (isSeriesEnd) {
            "Final Standings: You $humanScore - $aiScore Gemini"
          } else {
            "Round $currentRound complete • Standings: You $humanScore - $aiScore Gemini"
          },
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Gemini AI Commentary speech card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = GeminiViolet.copy(alpha = 0.08f),
          border = BorderStroke(1.dp, GeminiViolet.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = GeminiViolet,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Gemini Verdict Commentary",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = GeminiViolet
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "\"$commentary\"",
              style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Play multiple games in a row!
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Play Next Round button
          Button(
            onClick = onPlayNextRound,
            modifier = Modifier
              .weight(1.3f)
              .height(50.dp)
              .testTag("play_next_round_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isSeriesEnd) "Next Series" else "Next Round",
              fontWeight = FontWeight.Bold
            )
          }

          // Reset Series / New Match button
          OutlinedButton(
            onClick = onResetSeries,
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
              .testTag("reset_series_button"),
            shape = RoundedCornerShape(14.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Reset",
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
