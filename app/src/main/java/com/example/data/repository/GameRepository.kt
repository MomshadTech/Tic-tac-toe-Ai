package com.example.data.repository

import com.example.data.db.GameRoundDao
import com.example.data.db.GameRoundEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameRoundDao: GameRoundDao) {
  val allRounds: Flow<List<GameRoundEntity>> = gameRoundDao.getAllRounds()

  suspend fun insertRound(round: GameRoundEntity): Long {
    return gameRoundDao.insertRound(round)
  }

  suspend fun clearAllRounds() {
    gameRoundDao.clearAllRounds()
  }
}
