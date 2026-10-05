package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.GameRoundEntity
import com.example.data.remote.GeminiService
import com.example.data.repository.GameRepository
import com.example.logic.TicTacToeEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GameStatus {
  object InProgress : GameStatus
  data class RoundOver(
    val winner: String, // "HUMAN", "GEMINI", "DRAW"
    val winningLine: List<Int>,
    val commentary: String
  ) : GameStatus
  data class SeriesConcluded(
    val champion: String, // "HUMAN", "GEMINI"
    val finalHumanScore: Int,
    val finalAiScore: Int,
    val commentary: String
  ) : GameStatus
}

enum class SeriesTarget(val label: String, val winsNeeded: Int) {
  CONTINUOUS("Continuous", 0),
  BEST_OF_3("Best of 3", 2),
  BEST_OF_5("Best of 5", 3)
}

data class GameUiState(
  val board: List<String> = List(9) { "" },
  val isHumanTurn: Boolean = true,
  val isAiThinking: Boolean = false,
  val currentRound: Int = 1,
  val humanScore: Int = 0,
  val aiScore: Int = 0,
  val drawScore: Int = 0,
  val currentStreak: Int = 0, // positive for human streak, negative for AI streak
  val seriesTarget: SeriesTarget = SeriesTarget.CONTINUOUS,
  val gameStatus: GameStatus = GameStatus.InProgress,
  val latestAiCommentary: String = "Ready to test your wits against Gemini AI!",
  val isGeminiConnected: Boolean = true,
  val humanSymbol: String = "X",
  val aiSymbol: String = "O",
  val roundsPlayedInMatch: Int = 0
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val repository = GameRepository(database.gameRoundDao())
  private val geminiService = GeminiService()

  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  val allSavedRounds: StateFlow<List<GameRoundEntity>> = repository.allRounds.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  fun onCellClicked(index: Int) {
    val state = _uiState.value
    if (state.gameStatus !is GameStatus.InProgress) return
    if (!state.isHumanTurn || state.isAiThinking) return
    if (index !in 0..8 || state.board[index].isNotEmpty()) return

    // Apply human move
    val newBoard = state.board.toMutableList()
    newBoard[index] = state.humanSymbol

    val check = TicTacToeEngine.checkWinner(newBoard)
    if (check.winner != null) {
      handleRoundFinish(
        winner = "HUMAN",
        winningLine = check.winningLine,
        finalBoard = newBoard
      )
      return
    }

    if (check.isDraw) {
      handleRoundFinish(
        winner = "DRAW",
        winningLine = emptyList(),
        finalBoard = newBoard
      )
      return
    }

    // It's AI's turn!
    _uiState.value = state.copy(
      board = newBoard,
      isHumanTurn = false,
      isAiThinking = true
    )

    triggerAiMove(newBoard)
  }

  private fun triggerAiMove(currentBoard: List<String>) {
    viewModelScope.launch {
      val available = TicTacToeEngine.getAvailableMoves(currentBoard)
      if (available.isEmpty()) return@launch

      val aiResult = geminiService.getAiMove(currentBoard, available)
      val chosenMove = aiResult.move
      val updatedBoard = currentBoard.toMutableList()
      if (chosenMove in 0..8 && updatedBoard[chosenMove].isEmpty()) {
        updatedBoard[chosenMove] = _uiState.value.aiSymbol
      } else {
        val fallback = available.firstOrNull() ?: 0
        updatedBoard[fallback] = _uiState.value.aiSymbol
      }

      val check = TicTacToeEngine.checkWinner(updatedBoard)
      if (check.winner != null) {
        _uiState.value = _uiState.value.copy(
          board = updatedBoard,
          isAiThinking = false,
          latestAiCommentary = aiResult.commentary,
          isGeminiConnected = aiResult.isFromGemini
        )
        handleRoundFinish(
          winner = "GEMINI",
          winningLine = check.winningLine,
          finalBoard = updatedBoard,
          aiMoveCommentary = aiResult.commentary
        )
        return@launch
      }

      if (check.isDraw) {
        _uiState.value = _uiState.value.copy(
          board = updatedBoard,
          isAiThinking = false,
          latestAiCommentary = aiResult.commentary,
          isGeminiConnected = aiResult.isFromGemini
        )
        handleRoundFinish(
          winner = "DRAW",
          winningLine = emptyList(),
          finalBoard = updatedBoard,
          aiMoveCommentary = aiResult.commentary
        )
        return@launch
      }

      // Continue game, back to human turn
      _uiState.value = _uiState.value.copy(
        board = updatedBoard,
        isHumanTurn = true,
        isAiThinking = false,
        latestAiCommentary = aiResult.commentary,
        isGeminiConnected = aiResult.isFromGemini
      )
    }
  }

  private fun handleRoundFinish(
    winner: String,
    winningLine: List<Int>,
    finalBoard: List<String>,
    aiMoveCommentary: String = ""
  ) {
    val currentState = _uiState.value
    val newHumanScore = if (winner == "HUMAN") currentState.humanScore + 1 else currentState.humanScore
    val newAiScore = if (winner == "GEMINI") currentState.aiScore + 1 else currentState.aiScore
    val newDrawScore = if (winner == "DRAW") currentState.drawScore + 1 else currentState.drawScore

    val newStreak = when (winner) {
      "HUMAN" -> if (currentState.currentStreak >= 0) currentState.currentStreak + 1 else 1
      "GEMINI" -> if (currentState.currentStreak <= 0) currentState.currentStreak - 1 else -1
      else -> currentState.currentStreak
    }

    val roundNum = currentState.currentRound

    _uiState.value = currentState.copy(
      board = finalBoard,
      humanScore = newHumanScore,
      aiScore = newAiScore,
      drawScore = newDrawScore,
      currentStreak = newStreak,
      roundsPlayedInMatch = currentState.roundsPlayedInMatch + 1,
      gameStatus = GameStatus.RoundOver(
        winner = winner,
        winningLine = winningLine,
        commentary = aiMoveCommentary.ifBlank { "Analyzing match conclusion..." }
      )
    )

    // Query Gemini for post-round verdict commentary
    viewModelScope.launch {
      val verdict = geminiService.getVerdictCommentary(winner, finalBoard, roundNum)
      val currentStatus = _uiState.value.gameStatus

      // Check if series target reached (e.g. Best of 3 / 5)
      val targetWins = currentState.seriesTarget.winsNeeded
      val seriesOver = targetWins > 0 && (newHumanScore >= targetWins || newAiScore >= targetWins)

      if (seriesOver) {
        val champion = if (newHumanScore >= targetWins) "HUMAN" else "GEMINI"
        _uiState.value = _uiState.value.copy(
          latestAiCommentary = verdict,
          gameStatus = GameStatus.SeriesConcluded(
            champion = champion,
            finalHumanScore = newHumanScore,
            finalAiScore = newAiScore,
            commentary = verdict
          )
        )
      } else if (currentStatus is GameStatus.RoundOver) {
        _uiState.value = _uiState.value.copy(
          latestAiCommentary = verdict,
          gameStatus = currentStatus.copy(commentary = verdict)
        )
      }

      // Persist to Room Database
      val humanMoves = finalBoard.count { it == currentState.humanSymbol }
      val aiMoves = finalBoard.count { it == currentState.aiSymbol }
      repository.insertRound(
        GameRoundEntity(
          roundNumber = roundNum,
          winner = winner,
          boardState = finalBoard.joinToString(","),
          winningIndices = winningLine.joinToString(","),
          humanMovesCount = humanMoves,
          aiMovesCount = aiMoves,
          aiCommentary = verdict
        )
      )
    }
  }

  fun playNextRound() {
    val currentState = _uiState.value
    val nextRound = currentState.currentRound + 1
    // Alternate starter or human first
    val humanStarts = (nextRound % 2 != 0)

    _uiState.value = currentState.copy(
      board = List(9) { "" },
      currentRound = nextRound,
      isHumanTurn = humanStarts,
      isAiThinking = !humanStarts,
      gameStatus = GameStatus.InProgress,
      latestAiCommentary = if (humanStarts) "Round $nextRound! Your move first, human." else "Round $nextRound! Gemini AI has the first move."
    )

    if (!humanStarts) {
      triggerAiMove(List(9) { "" })
    }
  }

  fun startNewSeries() {
    val currentState = _uiState.value
    _uiState.value = currentState.copy(
      board = List(9) { "" },
      currentRound = 1,
      humanScore = 0,
      aiScore = 0,
      drawScore = 0,
      currentStreak = 0,
      roundsPlayedInMatch = 0,
      isHumanTurn = true,
      isAiThinking = false,
      gameStatus = GameStatus.InProgress,
      latestAiCommentary = "New series initiated! Make your opening move."
    )
  }

  fun setSeriesTarget(target: SeriesTarget) {
    _uiState.value = _uiState.value.copy(seriesTarget = target)
  }

  fun clearMatchHistory() {
    viewModelScope.launch {
      repository.clearAllRounds()
    }
  }
}
