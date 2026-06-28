package com.example.data.repository

import com.example.data.database.EsportsDao
import com.example.data.model.JoinedTournament
import com.example.data.model.Team
import com.example.data.model.BoostedGame
import com.example.data.model.GfxConfig
import kotlinx.coroutines.flow.Flow

class EsportsRepository(private val esportsDao: EsportsDao) {
    val allTeams: Flow<List<Team>> = esportsDao.getAllTeams()
    val allJoinedTournaments: Flow<List<JoinedTournament>> = esportsDao.getAllJoinedTournaments()
    val allBoostedGames: Flow<List<BoostedGame>> = esportsDao.getAllBoostedGames()
    val gfxConfig: Flow<GfxConfig?> = esportsDao.getGfxConfig()

    suspend fun insertTeam(team: Team): Long {
        return esportsDao.insertTeam(team)
    }

    suspend fun deleteTeam(teamId: Int) {
        esportsDao.deleteTeamById(teamId)
    }

    suspend fun joinTournament(tournament: JoinedTournament) {
        esportsDao.insertJoinedTournament(tournament)
    }

    suspend fun leaveTournament(id: Int) {
        esportsDao.deleteJoinedTournamentById(id)
    }

    // Game Booster methods
    suspend fun addBoostedGame(game: BoostedGame): Long {
        return esportsDao.insertBoostedGame(game)
    }

    suspend fun removeBoostedGame(gameId: Int) {
        esportsDao.deleteBoostedGameById(gameId)
    }

    suspend fun saveGfxConfig(config: GfxConfig) {
        esportsDao.saveGfxConfig(config)
    }
}
