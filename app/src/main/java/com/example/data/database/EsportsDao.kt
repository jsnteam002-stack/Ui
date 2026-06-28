package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.JoinedTournament
import com.example.data.model.Team
import com.example.data.model.BoostedGame
import com.example.data.model.GfxConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface EsportsDao {
    @Query("SELECT * FROM teams ORDER BY id DESC")
    fun getAllTeams(): Flow<List<Team>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Team): Long

    @Query("DELETE FROM teams WHERE id = :teamId")
    suspend fun deleteTeamById(teamId: Int)

    @Query("SELECT * FROM joined_tournaments ORDER BY registrationTime DESC")
    fun getAllJoinedTournaments(): Flow<List<JoinedTournament>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJoinedTournament(tournament: JoinedTournament)

    @Query("DELETE FROM joined_tournaments WHERE id = :id")
    suspend fun deleteJoinedTournamentById(id: Int)

    // Game Booster Queries
    @Query("SELECT * FROM boosted_games ORDER BY name ASC")
    fun getAllBoostedGames(): Flow<List<BoostedGame>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoostedGame(game: BoostedGame): Long

    @Query("DELETE FROM boosted_games WHERE id = :gameId")
    suspend fun deleteBoostedGameById(gameId: Int)

    @Query("SELECT * FROM gfx_settings WHERE id = 1 LIMIT 1")
    fun getGfxConfig(): Flow<GfxConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGfxConfig(config: GfxConfig)
}
