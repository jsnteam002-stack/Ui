package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val game: String,
    val captain: String,
    val members: String // Comma separated list of player tags
)

@Entity(tableName = "joined_tournaments")
data class JoinedTournament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentTitle: String,
    val game: String,
    val teamId: Int,
    val teamName: String,
    val registrationTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "boosted_games")
data class BoostedGame(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val packageName: String,
    val iconName: String, // String representation of a game icon preset
    val launchCount: Int = 0,
    val lastBoostedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "gfx_settings")
data class GfxConfig(
    @PrimaryKey val id: Int = 1, // Single active config
    val resolution: String = "1920x1080",
    val graphics: String = "HDR",
    val fps: String = "60 FPS",
    val style: String = "Colorful",
    val shadowEnabled: Boolean = true,
    val zeroLagMode: Boolean = true,
    val hardwareAcceleration: Boolean = true
)
