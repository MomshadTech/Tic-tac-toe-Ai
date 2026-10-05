package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.GameRoundEntity
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.HumanCyan
import com.example.ui.theme.VictoryGold

@Composable
fun MatchHistoryDialog(
  rounds: List<GameRoundEntity>,
  onDismiss: () -> Unit,
  onClearHistory: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.8f)
        .testTag("match_history_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Dialog header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Round History",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_history_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Dialog"
            )
          }
        }

        Text(
          text = "${rounds.size} games played in series",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (rounds.isEmpty()) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No recorded rounds yet.\nPlay a match to see results here!",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(rounds, key = { it.id }) { round ->
              RoundHistoryItem(round = round)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bottom actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (rounds.isNotEmpty()) {
            TextButton(
              onClick = onClearHistory,
              modifier = Modifier.testTag("clear_history_button")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.error
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Clear History",
                color = MaterialTheme.colorScheme.error
              )
            }
          } else {
            Spacer(modifier = Modifier.width(10.dp))
          }

          TextButton(onClick = onDismiss) {
            Text("Done")
          }
        }
      }
    }
  }
}

@Composable
private fun RoundHistoryItem(round: GameRoundEntity) {
  val (winnerColor, winnerText, icon) = when (round.winner) {
    "HUMAN" -> Triple(VictoryGold, "You Won", Icons.Default.EmojiEvents)
    "GEMINI" -> Triple(GeminiViolet, "Gemini Won", Icons.Default.SmartToy)
    else -> Triple(MaterialTheme.colorScheme.onSurfaceVariant, "Draw", Icons.Default.Handshake)
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .background(winnerColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = winnerColor,
              modifier = Modifier.size(16.dp)
            )
          }

          Text(
            text = "Round ${round.roundNumber}",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = winnerColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = winnerText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = winnerColor
          )
        }
      }

      if (round.aiCommentary.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "\"${round.aiCommentary}\"",
          style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2
        )
      }
    }
  }
}
