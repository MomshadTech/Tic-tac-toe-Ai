package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AiCommentaryBubble
import com.example.ui.components.MatchHistoryDialog
import com.example.ui.components.ScoreboardCard
import com.example.ui.components.TicTacToeBoard
import com.example.ui.components.WinningVerdictCard
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.HumanCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val savedRounds by viewModel.allSavedRounds.collectAsStateWithLifecycle()

  var showHistoryDialog by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  val winningLine = when (val status = uiState.gameStatus) {
    is GameStatus.RoundOver -> status.winningLine
    else -> emptyList()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = GeminiViolet,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Tic-Tac-Toe",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Gemini AI vs Human",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          // Match history button with count badge
          IconButton(
            onClick = { showHistoryDialog = true },
            modifier = Modifier.testTag("history_button")
          ) {
            BadgedBox(
              badge = {
                if (savedRounds.isNotEmpty()) {
                  Badge {
                    Text(savedRounds.size.toString())
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Match History"
              )
            }
          }

          // Reset match button
          IconButton(
            onClick = { showResetConfirmDialog = true },
            modifier = Modifier.testTag("reset_action_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reset Series"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 500.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Series mode selection chips: Continuous, Best of 3, Best of 5
          SeriesTargetSelector(
            currentTarget = uiState.seriesTarget,
            onTargetSelected = { viewModel.setSeriesTarget(it) }
          )

          // Scoreboard Card
          ScoreboardCard(
            currentRound = uiState.currentRound,
            humanScore = uiState.humanScore,
            aiScore = uiState.aiScore,
            drawScore = uiState.drawScore,
            currentStreak = uiState.currentStreak,
            seriesTarget = uiState.seriesTarget
          )

          // AI Commentary / Status Bubble
          AiCommentaryBubble(
            isAiThinking = uiState.isAiThinking,
            isHumanTurn = uiState.isHumanTurn,
            commentary = uiState.latestAiCommentary
          )

          // 3x3 Tic-Tac-Toe Board
          TicTacToeBoard(
            board = uiState.board,
            winningIndices = winningLine,
            isInteractive = uiState.gameStatus is GameStatus.InProgress && uiState.isHumanTurn && !uiState.isAiThinking,
            onCellClicked = { index -> viewModel.onCellClicked(index) }
          )

          // Winning Verdict Display (shown when round finishes or series concludes)
          if (uiState.gameStatus !is GameStatus.InProgress) {
            WinningVerdictCard(
              gameStatus = uiState.gameStatus,
              currentRound = uiState.currentRound,
              humanScore = uiState.humanScore,
              aiScore = uiState.aiScore,
              onPlayNextRound = {
                if (uiState.gameStatus is GameStatus.SeriesConcluded) {
                  viewModel.startNewSeries()
                } else {
                  viewModel.playNextRound()
                }
              },
              onResetSeries = { viewModel.startNewSeries() }
            )
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }

  // History Dialog
  if (showHistoryDialog) {
    MatchHistoryDialog(
      rounds = savedRounds,
      onDismiss = { showHistoryDialog = false },
      onClearHistory = { viewModel.clearMatchHistory() }
    )
  }

  // Reset Confirmation Dialog
  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = { Text("Reset Series Score?") },
      text = { Text("This will reset the current series score back to 0 - 0 and start from Round 1. Your saved history in the log will remain preserved.") },
      confirmButton = {
        TextButton(
          onClick = {
            showResetConfirmDialog = false
            viewModel.startNewSeries()
          },
          modifier = Modifier.testTag("confirm_reset_button")
        ) {
          Text("Reset Series", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun SeriesTargetSelector(
  currentTarget: SeriesTarget,
  onTargetSelected: (SeriesTarget) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically
  ) {
    SeriesTarget.entries.forEach { target ->
      val isSelected = target == currentTarget
      FilterChip(
        selected = isSelected,
        onClick = { onTargetSelected(target) },
        label = {
          Text(
            text = target.label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.testTag("series_mode_${target.name.lowercase()}")
      )
    }
  }
}
