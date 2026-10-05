package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_rounds")
data class GameRoundEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val roundNumber: Int,
  val winner: String, // "HUMAN", "GEMINI", "DRAW"
  val boardState: String, // e.g. "X,O,,X,O,,,X,"
  val winningIndices: String = "", // e.g. "0,4,8"
  val humanMovesCount: Int,
  val aiMovesCount: Int,
  val aiCommentary: String = "",
  val timestamp: Long = System.currentTimeMillis()
)
