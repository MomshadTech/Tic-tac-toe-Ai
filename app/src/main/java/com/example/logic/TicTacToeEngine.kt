package com.example.logic

object TicTacToeEngine {
  val WINNING_COMBINATIONS = listOf(
    listOf(0, 1, 2),
    listOf(3, 4, 5),
    listOf(6, 7, 8),
    listOf(0, 3, 6),
    listOf(1, 4, 7),
    listOf(2, 5, 8),
    listOf(0, 4, 8),
    listOf(2, 4, 6)
  )

  data class WinResult(
    val winner: String?, // "X", "O", or null
    val winningLine: List<Int> = emptyList(),
    val isDraw: Boolean = false
  )

  fun checkWinner(board: List<String>): WinResult {
    for (combo in WINNING_COMBINATIONS) {
      val a = board[combo[0]]
      val b = board[combo[1]]
      val c = board[combo[2]]
      if (a.isNotEmpty() && a == b && b == c) {
        return WinResult(winner = a, winningLine = combo, isDraw = false)
      }
    }
    val isDraw = board.all { it.isNotEmpty() }
    return WinResult(winner = null, winningLine = emptyList(), isDraw = isDraw)
  }

  fun getAvailableMoves(board: List<String>): List<Int> {
    return board.indices.filter { board[it].isEmpty() }
  }

  fun getBestMove(board: List<String>, aiPlayer: String = "O", humanPlayer: String = "X"): Int {
    val available = getAvailableMoves(board)
    if (available.isEmpty()) return -1

    // 1. Check if AI can win in 1 move
    for (pos in available) {
      val mutableBoard = board.toMutableList()
      mutableBoard[pos] = aiPlayer
      if (checkWinner(mutableBoard).winner == aiPlayer) {
        return pos
      }
    }

    // 2. Check if Human could win in 1 move and block them
    for (pos in available) {
      val mutableBoard = board.toMutableList()
      mutableBoard[pos] = humanPlayer
      if (checkWinner(mutableBoard).winner == humanPlayer) {
        return pos
      }
    }

    // 3. Take Center if open
    if (4 in available) return 4

    // 4. Take random corner if open
    val corners = listOf(0, 2, 6, 8).filter { it in available }
    if (corners.isNotEmpty()) {
      return corners.shuffled().first()
    }

    // 5. Take any remaining side
    return available.shuffled().first()
  }

  fun formatBoardForPrompt(board: List<String>): String {
    val posNames = listOf(
      "0:Top-Left", "1:Top-Center", "2:Top-Right",
      "3:Mid-Left", "4:Center", "5:Mid-Right",
      "6:Bottom-Left", "7:Bottom-Center", "8:Bottom-Right"
    )
    return board.mapIndexed { index, cell ->
      val value = if (cell.isEmpty()) "Empty" else cell
      "${posNames[index]}=$value"
    }.joinToString(", ")
  }
}
