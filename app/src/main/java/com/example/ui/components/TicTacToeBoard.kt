package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.GeminiVioletLight
import com.example.ui.theme.HumanCyan
import com.example.ui.theme.HumanCyanLight
import com.example.ui.theme.VictoryGold
import com.example.ui.theme.VictoryGoldLight

@Composable
fun TicTacToeBoard(
  board: List<String>,
  winningIndices: List<Int>,
  isInteractive: Boolean,
  onCellClicked: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "win_glow")
  val glowAlpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(700, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_alpha"
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .sizeIn(maxWidth = 380.dp, maxHeight = 380.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      for (row in 0..2) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          for (col in 0..2) {
            val index = row * 3 + col
            val cellValue = board.getOrElse(index) { "" }
            val isWinningCell = index in winningIndices

            BoardCell(
              value = cellValue,
              isWinning = isWinningCell,
              winningGlowAlpha = glowAlpha,
              isClickable = isInteractive && cellValue.isEmpty(),
              index = index,
              onClick = { onCellClicked(index) },
              modifier = Modifier
                .weight(1f)
                .fillMaxSize()
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BoardCell(
  value: String,
  isWinning: Boolean,
  winningGlowAlpha: Float,
  isClickable: Boolean,
  index: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interactionSource = remember { MutableInteractionSource() }

  val cellBorder = when {
    isWinning -> BorderStroke(
      3.dp,
      Brush.linearGradient(
        listOf(
          VictoryGold.copy(alpha = winningGlowAlpha),
          VictoryGoldLight.copy(alpha = winningGlowAlpha)
        )
      )
    )
    isClickable -> BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
    else -> BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    )
  }

  val containerColor = when {
    isWinning -> VictoryGold.copy(alpha = 0.15f)
    value.isNotEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
  }

  Card(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .minimumInteractiveComponentSize()
      .testTag("cell_$index")
      .semantics {
        contentDescription = "Cell $index, ${if (value.isEmpty()) "empty" else "contains $value"}"
      }
      .then(
        if (isClickable) {
          Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = HumanCyan),
            onClick = onClick
          )
        } else Modifier
      ),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = cellBorder,
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (isWinning) 4.dp else 1.dp
    )
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      if (value == "X") {
        DrawCross(modifier = Modifier.fillMaxSize(0.6f))
      } else if (value == "O") {
        DrawNought(modifier = Modifier.fillMaxSize(0.6f))
      }
    }
  }
}

@Composable
private fun DrawCross(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val strokeWidth = size.width * 0.14f
    val pad = size.width * 0.1f

    // Diagonal 1: top-left to bottom-right
    drawLine(
      color = HumanCyan,
      start = Offset(pad, pad),
      end = Offset(size.width - pad, size.height - pad),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )
    // Diagonal 2: top-right to bottom-left
    drawLine(
      color = HumanCyanLight,
      start = Offset(size.width - pad, pad),
      end = Offset(pad, size.height - pad),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )
  }
}

@Composable
private fun DrawNought(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val strokeWidth = size.width * 0.14f
    val pad = size.width * 0.1f
    val diameter = size.width - (pad * 2)

    drawArc(
      brush = Brush.sweepGradient(
        listOf(GeminiViolet, GeminiVioletLight, GeminiViolet)
      ),
      startAngle = 0f,
      sweepAngle = 360f,
      useCenter = false,
      topLeft = Offset(pad, pad),
      size = Size(diameter, diameter),
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
  }
}
