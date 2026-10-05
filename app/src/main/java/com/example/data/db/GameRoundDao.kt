package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRoundDao {
  @Query("SELECT * FROM game_rounds ORDER BY id DESC")
  fun getAllRounds(): Flow<List<GameRoundEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRound(round: GameRoundEntity): Long

  @Query("DELETE FROM game_rounds")
  suspend fun clearAllRounds()

  @Query("SELECT COUNT(*) FROM game_rounds")
  suspend fun getCount(): Int
}
