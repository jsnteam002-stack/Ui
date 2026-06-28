package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.JoinedTournament
import com.example.data.model.Team
import com.example.data.model.BoostedGame
import com.example.data.model.GfxConfig
import com.example.data.repository.EsportsRepository
import com.example.data.api.GeminiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

// Client-facing Esports data structures
data class EsportsTournament(
    val id: String,
    val title: String,
    val game: String,
    val prizePool: String,
    val status: String, // "Live", "Upcoming", "Completed"
    val date: String,
    val teamLimit: Int = 16,
    val joinedTeamsCount: Int = 12
)

data class EsportsMatch(
    val id: String,
    val teamA: String,
    val teamB: String,
    val scoreA: Int,
    val scoreB: Int,
    val status: String, // "Live", "Upcoming", "Finished"
    val game: String,
    val tournamentName: String,
    val time: String
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class EsportsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EsportsRepository

    // Performance statistics
    private val _ramUsage = MutableStateFlow(68)
    val ramUsage: StateFlow<Int> = _ramUsage.asStateFlow()

    private val _cpuTemperature = MutableStateFlow(42)
    val cpuTemperature: StateFlow<Int> = _cpuTemperature.asStateFlow()

    private val _networkPing = MutableStateFlow(24)
    val networkPing: StateFlow<Int> = _networkPing.asStateFlow()

    // Booster Actions Status State
    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _isCoolingDown = MutableStateFlow(false)
    val isCoolingDown: StateFlow<Boolean> = _isCoolingDown.asStateFlow()

    private val _isTestingNetwork = MutableStateFlow(false)
    val isTestingNetwork: StateFlow<Boolean> = _isTestingNetwork.asStateFlow()

    private val _boostOutputLogs = MutableStateFlow<List<String>>(emptyList())
    val boostOutputLogs: StateFlow<List<String>> = _boostOutputLogs.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = EsportsRepository(database.esportsDao())

        // Periodically fluctuate stats slightly to feel organic and live
        viewModelScope.launch {
            while (true) {
                delay(3000)
                if (!_isBoosting.value && !_isCoolingDown.value && !_isTestingNetwork.value) {
                    _ramUsage.value = (60 + Random.nextInt(-4, 6)).coerceIn(40, 95)
                    _cpuTemperature.value = (40 + Random.nextInt(-2, 4)).coerceIn(30, 85)
                    _networkPing.value = (22 + Random.nextInt(-3, 5)).coerceIn(10, 150)
                }
            }
        }

        // Initialize default GfxConfig in DB if it doesn't exist
        viewModelScope.launch {
            repository.saveGfxConfig(GfxConfig())
        }

        // Add default gaming titles into Room library if empty
        viewModelScope.launch {
            delay(500)
            repository.allBoostedGames.collect { games ->
                if (games.isEmpty()) {
                    repository.addBoostedGame(BoostedGame(name = "PUBG Mobile", packageName = "com.tencent.ig", iconName = "pubg"))
                    repository.addBoostedGame(BoostedGame(name = "Genshin Impact", packageName = "com.miHoYo.GenshinImpact", iconName = "genshin"))
                    repository.addBoostedGame(BoostedGame(name = "Mobile Legends", packageName = "com.mobile.legends", iconName = "mlbb"))
                    repository.addBoostedGame(BoostedGame(name = "Free Fire", packageName = "com.dts.freefireth", iconName = "freefire"))
                }
            }
        }
    }

    // Database states
    val teams: StateFlow<List<Team>> = repository.allTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val joinedTournaments: StateFlow<List<JoinedTournament>> = repository.allJoinedTournaments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val boostedGames: StateFlow<List<BoostedGame>> = repository.allBoostedGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gfxConfig: StateFlow<GfxConfig?> = repository.gfxConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GfxConfig())

    // UI and AI Chat state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Welcome to Nex Arena AI Performance Analyst! I am your esports & gaming booster co-pilot. Ask me anything about tournament predictions, team rosters, GFX tuning configurations, hardware performance, or latency optimizations!",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Static Esports Content for immersive experience
    val tournaments = listOf(
        EsportsTournament("t1", "Vanguard Challengers Cup", "Valorant", "$50,000", "Live", "June 25 - July 5"),
        EsportsTournament("t2", "Apex Legends Global Arena", "Apex Legends", "$120,000", "Upcoming", "July 12 - July 20"),
        EsportsTournament("t3", "Championship Elite Series", "CS2", "$250,000", "Upcoming", "August 1 - August 15"),
        EsportsTournament("t4", "Legends Summoners Arena", "League of Legends", "$100,000", "Live", "June 20 - July 3"),
        EsportsTournament("t5", "Dota 2 Immortal Clash", "Dota 2", "$300,000", "Completed", "June 10 - June 18")
    )

    val matches = listOf(
        EsportsMatch("m1", "NEX GEN Esports", "Sentinels Elite", 2, 1, "Live", "Valorant", "Vanguard Challengers Cup", "Map 3 - Live Now"),
        EsportsMatch("m2", "G2 Esports", "Natus Vincere", 1, 0, "Live", "CS2", "Championship Elite Series", "Map 2 - Live Now"),
        EsportsMatch("m3", "T1 Gaming", "Gen.G Esports", 0, 0, "Upcoming", "League of Legends", "Legends Summoners Arena", "Starts in 2h"),
        EsportsMatch("m4", "Fnatic Prime", "Team Liquid", 0, 0, "Upcoming", "Valorant", "Vanguard Challengers Cup", "Starts in 5h"),
        EsportsMatch("m5", "Faze Clan", "Team Vitality", 3, 2, "Finished", "CS2", "Championship Elite Series", "BO5 Final - Ended")
    )

    // DB functions
    fun createTeam(name: String, game: String, captain: String, members: String) {
        viewModelScope.launch {
            repository.insertTeam(Team(name = name, game = game, captain = captain, members = members))
        }
    }

    fun deleteTeam(teamId: Int) {
        viewModelScope.launch {
            repository.deleteTeam(teamId)
        }
    }

    fun joinTournament(tournamentTitle: String, game: String, teamId: Int, teamName: String) {
        viewModelScope.launch {
            repository.joinTournament(
                JoinedTournament(
                    tournamentTitle = tournamentTitle,
                    game = game,
                    teamId = teamId,
                    teamName = teamName
                )
            )
        }
    }

    fun leaveTournament(joinedId: Int) {
        viewModelScope.launch {
            repository.leaveTournament(joinedId)
        }
    }

    // Game Booster Functions
    fun addGame(name: String, packageName: String, iconName: String) {
        viewModelScope.launch {
            repository.addBoostedGame(BoostedGame(name = name, packageName = packageName, iconName = iconName))
        }
    }

    fun deleteGame(gameId: Int) {
        viewModelScope.launch {
            repository.removeBoostedGame(gameId)
        }
    }

    fun updateGfxConfig(resolution: String, graphics: String, fps: String, style: String, shadow: Boolean, zeroLag: Boolean, hwAccel: Boolean) {
        viewModelScope.launch {
            repository.saveGfxConfig(
                GfxConfig(
                    resolution = resolution,
                    graphics = graphics,
                    fps = fps,
                    style = style,
                    shadowEnabled = shadow,
                    zeroLagMode = zeroLag,
                    hardwareAcceleration = hwAccel
                )
            )
        }
    }

    // Live Booster Simulation Routines
    fun triggerNexBoost() {
        if (_isBoosting.value) return
        _isBoosting.value = true
        _boostOutputLogs.value = emptyList()

        viewModelScope.launch {
            val steps = listOf(
                "Initializing Nex Arena Engine...",
                "Scanning memory blocks for cached telemetry data...",
                "Clearing background services and system caches...",
                "Stopping duplicate network listeners...",
                "Allocating maximum heap limit to gaming processes...",
                "Applying CPU governor state adjustments...",
                "Injecting GPU booster shaders..."
            )

            for (step in steps) {
                _boostOutputLogs.value = _boostOutputLogs.value + step
                delay(400)
            }

            // Lower performance metrics significantly as proof of optimization
            _ramUsage.value = (35 + Random.nextInt(5))
            _cpuTemperature.value = (32 + Random.nextInt(3))
            _networkPing.value = (14 + Random.nextInt(3))

            _boostOutputLogs.value = _boostOutputLogs.value + "🎉 NEX SPEED BOOST APPLIED SUCCESSFULLY!"
            delay(1000)
            _isBoosting.value = false
        }
    }

    fun coolCpu() {
        if (_isCoolingDown.value) return
        _isCoolingDown.value = true
        _boostOutputLogs.value = emptyList()

        viewModelScope.launch {
            val steps = listOf(
                "Monitoring system core frequencies...",
                "Pinpointing high-load background threads...",
                "Underclocking dormant CPU cores...",
                "Activating cooling core algorithms...",
                "Reducing GPU processing pressure..."
            )

            for (step in steps) {
                _boostOutputLogs.value = _boostOutputLogs.value + step
                delay(500)
            }

            _cpuTemperature.value = (31 + Random.nextInt(3))
            _boostOutputLogs.value = _boostOutputLogs.value + "❄️ CPU Core cooled down to normal temperature bounds!"
            delay(800)
            _isCoolingDown.value = false
        }
    }

    fun testNetwork() {
        if (_isTestingNetwork.value) return
        _isTestingNetwork.value = true
        _boostOutputLogs.value = emptyList()

        viewModelScope.launch {
            val steps = listOf(
                "Locating nearest Nex Arena gaming servers...",
                "Pinging Frankfurt lines (E-Sport line 1)...",
                "Pinging Singapore lines (E-Sport line 2)...",
                "Analyzing packet loss parameters...",
                "Rerouting game traffic to ultra-low latency gateway..."
            )

            for (step in steps) {
                _boostOutputLogs.value = _boostOutputLogs.value + step
                delay(600)
            }

            _networkPing.value = (12 + Random.nextInt(4))
            _boostOutputLogs.value = _boostOutputLogs.value + "⚡ Latency Optimized! Average ping locked at ${_networkPing.value} ms"
            delay(800)
            _isTestingNetwork.value = false
        }
    }

    // AI Functions
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage(text = text, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        _isAiLoading.value = true
        viewModelScope.launch {
            val systemPrompt = """
                You are the 'Nex Arena AI Gaming Performance Coach & Hardware Analyst'. 
                You are a high-energy gaming pro who knows Android graphics configurations, GFX, frame rates (FPS), latency optimization, thermal cooling, and competitive esports match analysis.
                Keep your responses gaming-themed, sharp, concise, and incredibly technical yet easy to understand.
                Always format your response with clean Markdown for beautiful display in Jetpack Compose.
            """.trimIndent()

            val aiAnswer = GeminiClient.generateContent(prompt = text, systemInstruction = systemPrompt)
            val aiMsg = ChatMessage(text = aiAnswer, isUser = false)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    fun predictMatchOutcome(match: EsportsMatch) {
        _isAiLoading.value = true
        
        val promptText = "Analyze and predict the match outcome for ${match.teamA} vs ${match.teamB} playing ${match.game} in ${match.tournamentName}."
        val userMsg = ChatMessage(text = promptText, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            val systemPrompt = """
                You are the 'Nex Arena AI Match Predictor'. You analyze esports teams, game meta, and player states.
                Provide a premium, analytical prediction for the requested matchup with detailed analytics (win percentage, win conditions, key player matchups, and map veto suggestions).
                Make it look highly professional and immersive. End with a predicted score! 
                Format your output with clean Markdown (bold headings, bullet points).
            """.trimIndent()

            val prediction = GeminiClient.generateContent(prompt = promptText, systemInstruction = systemPrompt)
            val aiMsg = ChatMessage(text = prediction, isUser = false)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }
}
