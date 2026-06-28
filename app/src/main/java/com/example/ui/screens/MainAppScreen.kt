package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.model.Team
import com.example.data.model.BoostedGame
import com.example.data.model.GfxConfig
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Screen Routes
object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val GFX_TOOL = "gfx_tool"
    const val AI_COACH = "ai_coach"
    const val ESPORTS = "esports"
}

// Bottom Navigation items
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    object Dashboard : BottomNavItem(Routes.DASHBOARD, "Booster", Icons.Filled.Speed, Icons.Outlined.Speed, "nav_booster")
    object GfxTool : BottomNavItem(Routes.GFX_TOOL, "GFX Tuning", Icons.Filled.Tune, Icons.Outlined.Tune, "nav_gfx")
    object AiCoach : BottomNavItem(Routes.AI_COACH, "AI Coach", Icons.Filled.Psychology, Icons.Outlined.Psychology, "nav_ai")
    object Esports : BottomNavItem(Routes.ESPORTS, "Esports Hub", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports, "nav_esports")
}

@Composable
fun MainAppNavigation(
    viewModel: EsportsViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NexBlack,
        bottomBar = {
            if (currentRoute != Routes.SPLASH) {
                NavigationBar(
                    containerColor = NexDarkGray,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .border(
                            width = 0.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(NexCyan.copy(alpha = 0.15f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                ) {
                    val items = listOf(
                        BottomNavItem.Dashboard,
                        BottomNavItem.GfxTool,
                        BottomNavItem.AiCoach,
                        BottomNavItem.Esports
                    )
                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            modifier = Modifier.testTag(item.tag),
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (selected) NexCyan else NexLightGray
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) NexCyan else NexLightGray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NexElectricBlue.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onSplashCompleted = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }

            composable(Routes.DASHBOARD) {
                DashboardBoosterScreen(viewModel = viewModel)
            }

            composable(Routes.GFX_TOOL) {
                GfxTuningScreen(viewModel = viewModel)
            }

            composable(Routes.AI_COACH) {
                AiCoachScreen(viewModel = viewModel)
            }

            composable(Routes.ESPORTS) {
                EsportsHubScreen(viewModel = viewModel, navController = navController)
            }
        }
    }
}

// ==========================================
// SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(onSplashCompleted: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2200)
        onSplashCompleted()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexBlack)
            .drawBehind {
                val brush = Brush.radialGradient(
                    colors = listOf(NexElectricBlue.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.width * 0.9f
                )
                drawCircle(brush = brush, radius = size.width * 0.9f)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(NexCyan, NexElectricBlue)),
                        shape = CircleShape
                    )
                    .padding(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "Nex Arena Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NEX ARENA",
                fontFamily = FontFamily.Monospace,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = NexCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "NEXT-GEN GAME TUNER & ESPORTS HUB",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = TextGray,
                letterSpacing = 2.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = NexCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Optimizing Gaming Shaders...",
                color = NexCyan.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================
// BOOSTER DASHBOARD HUB
// ==========================================
@Composable
fun DashboardBoosterScreen(viewModel: EsportsViewModel) {
    val ram by viewModel.ramUsage.collectAsStateWithLifecycle()
    val temp by viewModel.cpuTemperature.collectAsStateWithLifecycle()
    val ping by viewModel.networkPing.collectAsStateWithLifecycle()

    val isBoosting by viewModel.isBoosting.collectAsStateWithLifecycle()
    val isCoolingDown by viewModel.isCoolingDown.collectAsStateWithLifecycle()
    val isTestingNetwork by viewModel.isTestingNetwork.collectAsStateWithLifecycle()
    val boostLogs by viewModel.boostOutputLogs.collectAsStateWithLifecycle()
    val games by viewModel.boostedGames.collectAsStateWithLifecycle()

    var showAddGameDialog by remember { mutableStateOf(false) }
    var activeLaunchingGame by remember { mutableStateOf<BoostedGame?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Pulsing booster outer ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "booster_ring")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, bottom = 40.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🚀 NEX BOOST",
                            color = NexCyan,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "Real-time hardware speed optimizing cockpit",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(NexDarkGray, CircleShape)
                            .border(0.5.dp, NexCyan.copy(alpha = 0.4f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "STATUS: ARMED",
                            color = NexCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Central Pulsating Speed Booster Ring
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glowing background aura
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            NexCyan.copy(alpha = 0.15f),
                                            NexElectricBlue.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.width * 0.8f
                                )
                            }
                    )

                    // Rotating animated border
                    Box(
                        modifier = Modifier
                            .size((140f * pulseScale).dp)
                            .border(
                                width = 1.dp,
                                brush = Brush.sweepGradient(listOf(NexCyan, NexElectricBlue, NexCyan)),
                                shape = CircleShape
                            )
                    )

                    // Core Button
                    Surface(
                        onClick = { viewModel.triggerNexBoost() },
                        shape = CircleShape,
                        color = NexDarkGray,
                        modifier = Modifier
                            .size(130.dp)
                            .border(2.dp, NexCyan, CircleShape)
                            .testTag("core_boost_btn")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = "Boost Icon",
                                tint = NexCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBoosting) "BOOSTING" else "NEX BOOST",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "TAP TO OPTIMIZE",
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Realtime Gauge Counters Panel
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // RAM Panel
                    GaugeCard(
                        title = "RAM",
                        value = "$ram%",
                        subtitle = "Total: 12GB LPDDR5",
                        progress = ram / 100f,
                        color = NexCyan,
                        modifier = Modifier.weight(1f)
                    )

                    // CPU Panel
                    GaugeCard(
                        title = "CPU Core",
                        value = "$temp°C",
                        subtitle = "Thermal Limit 85°",
                        progress = (temp - 20) / 65f,
                        color = Color.Red,
                        modifier = Modifier.weight(1f)
                    )

                    // Ping Latency Panel
                    GaugeCard(
                        title = "Network",
                        value = "${ping}ms",
                        subtitle = "Gaming Gateway",
                        progress = (150 - ping).coerceAtLeast(0) / 140f,
                        color = Color.Green,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Hardware Optimization Tools Menu
            item {
                Text(
                    text = "⚙️ System Optimization Modules",
                    color = NexCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // CPU Cooler Core Trigger
                    ActionButtonTile(
                        title = "CPU Cooler",
                        desc = "Underclock dormant cores",
                        icon = Icons.Filled.AcUnit,
                        color = NexCyan,
                        onClick = { viewModel.coolCpu() },
                        modifier = Modifier.weight(1f)
                    )

                    // Ping Latency Enhancer
                    ActionButtonTile(
                        title = "Ping Stabilizer",
                        desc = "Optimize packet route",
                        icon = Icons.Filled.WifiTethering,
                        color = NexElectricBlue,
                        onClick = { viewModel.testNetwork() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Active Games Dashboard Panel
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎮 My Game Library",
                        color = NexCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { showAddGameDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NexDarkGray, contentColor = NexCyan),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("add_custom_game")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Game", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Game", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Games Grid / Scroller
            item {
                if (games.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexDarkGray)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No games added yet. Tap 'Add Game' to build your custom arena library!",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(games) { game ->
                            GameLibraryTile(
                                game = game,
                                onLaunchClick = {
                                    activeLaunchingGame = game
                                    scope.launch {
                                        delay(3000) // Simulated game loading/booster process
                                        activeLaunchingGame = null
                                        Toast.makeText(context, "Performance Boosted! ${game.name} launched.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onDeleteClick = {
                                    viewModel.deleteGame(game.id)
                                    Toast.makeText(context, "Removed ${game.name} from Library", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Overlay simulated booster sequence
        if (isBoosting || isCoolingDown || isTestingNetwork) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NexDarkGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexCyan, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Dynamic indicator
                        CircularProgressIndicator(
                            color = NexCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when {
                                isBoosting -> "NEX SPEED SHADER ACTIVE"
                                isCoolingDown -> "ACTIVATING TEMPERATURE FLUID SHIELD"
                                else -> "LATENCY GATEWAY ROUTER ALIGNED"
                            },
                            color = NexCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Terminal Style Logs Scroller
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NexBlack)
                                .border(0.5.dp, NexLightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            LazyColumn(reverseLayout = true) {
                                items(boostLogs.reversed()) { log ->
                                    Text(
                                        text = "> $log",
                                        color = Color.Green,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Left
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay Game Launch optimizer HUD
        if (activeLaunchingGame != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(1.dp, NexCyan, CircleShape)
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports,
                            contentDescription = "Game Launching",
                            tint = NexCyan,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "OPTIMIZING PERFORMANCE SLOTS",
                        color = NexCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Launching ${activeLaunchingGame!!.name} in hyper-boost mode...",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    LinearProgressIndicator(
                        color = NexCyan,
                        trackColor = NexDarkGray,
                        modifier = Modifier
                            .width(200.dp)
                            .height(4.dp)
                    )
                }
            }
        }

        // Add custom game dialog
        if (showAddGameDialog) {
            AddGameDialog(
                onDismiss = { showAddGameDialog = false },
                onAddGame = { name, pkg, icon ->
                    viewModel.addGame(name, pkg, icon)
                    showAddGameDialog = false
                    Toast.makeText(context, "Added $name to your Arena library!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// Custom Gauge component
@Composable
fun GaugeCard(
    title: String,
    value: String,
    subtitle: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexDarkGray),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Simulated small linear indicator representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(NexBlack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .background(color)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = subtitle,
                color = TextGray,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Custom Grid Item Actions
@Composable
fun ActionButtonTile(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = NexDarkGray,
        modifier = modifier.border(0.5.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    color = TextGray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// Game Library Card Game Asset representation
@Composable
fun GameLibraryTile(
    game: BoostedGame,
    onLaunchClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NexDarkGray),
        modifier = Modifier
            .width(115.dp)
            .border(0.5.dp, NexCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(NexBlack, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (game.iconName) {
                        "pubg" -> Icons.Filled.MilitaryTech
                        "genshin" -> Icons.Filled.Cloud
                        "mlbb" -> Icons.Filled.Star
                        "freefire" -> Icons.Filled.LocalFireDepartment
                        else -> Icons.Filled.SportsEsports
                    },
                    contentDescription = game.name,
                    tint = NexCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = game.name,
                color = TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onLaunchClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NexCyan, contentColor = NexBlack),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .testTag("launch_${game.id}")
                ) {
                    Text("PLAY", fontSize = 9.sp, fontWeight = FontWeight.Black)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// Dialog to add custom games to library list
@Composable
fun AddGameDialog(
    onDismiss: () -> Unit,
    onAddGame: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var pkg by remember { mutableStateOf("") }
    var selectedIconIndex by remember { mutableStateOf(0) }
    val iconPresets = listOf("pubg", "genshin", "mlbb", "freefire", "generic")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexDarkGray,
        title = {
            Text(text = "Reroute Game to Library", color = NexCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Game Display Title", color = NexLightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NexCyan,
                        unfocusedBorderColor = NexLightGray
                    ),
                    modifier = Modifier.testTag("new_game_title_input")
                )

                OutlinedTextField(
                    value = pkg,
                    onValueChange = { pkg = it },
                    label = { Text("Android Package ID (e.g. com.game)", color = NexLightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NexCyan,
                        unfocusedBorderColor = NexLightGray
                    ),
                    modifier = Modifier.testTag("new_game_pkg_input")
                )

                Text(text = "Choose Game Preset Icon:", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    iconPresets.forEachIndexed { idx, item ->
                        val isSelected = idx == selectedIconIndex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(if (isSelected) NexElectricBlue else NexBlack, RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSelected) NexCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { selectedIconIndex = idx }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (item) {
                                    "pubg" -> Icons.Filled.MilitaryTech
                                    "genshin" -> Icons.Filled.Cloud
                                    "mlbb" -> Icons.Filled.Star
                                    "freefire" -> Icons.Filled.LocalFireDepartment
                                    else -> Icons.Filled.SportsEsports
                                },
                                contentDescription = item,
                                tint = if (isSelected) TextWhite else NexCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddGame(title, pkg.ifBlank { "com.nexarena.${title.lowercase()}" }, iconPresets[selectedIconIndex])
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexCyan, contentColor = NexBlack),
                enabled = title.isNotBlank()
            ) {
                Text("Lock in Library")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

// ==========================================
// GFX TUNER SCREEN
// ==========================================
@Composable
fun GfxTuningScreen(viewModel: EsportsViewModel) {
    val activeConfig by viewModel.gfxConfig.collectAsStateWithLifecycle()
    var isCompilingGfx by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Local configuration states initialized with current config values
    var resolution by remember { mutableStateOf("1920x1080") }
    var graphics by remember { mutableStateOf("HDR") }
    var fpsLimit by remember { mutableStateOf("60 FPS") }
    var stylePreset by remember { mutableStateOf("Colorful") }
    var isShadowEnabled by remember { mutableStateOf(true) }
    var isZeroLagEnabled by remember { mutableStateOf(true) }
    var isHwAccelEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(activeConfig) {
        activeConfig?.let {
            resolution = it.resolution
            graphics = it.graphics
            fpsLimit = it.fps
            stylePreset = it.style
            isShadowEnabled = it.shadowEnabled
            isZeroLagEnabled = it.zeroLagMode
            isHwAccelEnabled = it.hardwareAcceleration
        }
    }

    val resOptions = listOf("1280x720 (Lite)", "1920x1080 (HD)", "2560x1440 (2K)", "3840x2160 (4K UHD)")
    val graphOptions = listOf("Smooth", "Balanced", "HD", "HDR", "Ultra HD")
    val fpsOptions = listOf("30 FPS", "45 FPS", "60 FPS (Pro)", "90 FPS (Extreme)", "120 FPS (Elite)")
    val styleOptions = listOf("Classic", "Colorful", "Realistic", "Soft", "Movie")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "🛠️ GFX Tuning Engine",
                        color = NexCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Unlock Extreme Framerates & optimize shadow compilation",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Gfx Selection items
            item {
                SelectorDropdownCard(
                    title = "Resolution Profile",
                    options = resOptions,
                    selectedOption = resolution,
                    onOptionSelected = { resolution = it }
                )
            }

            item {
                SelectorDropdownCard(
                    title = "Graphics Rendering",
                    options = graphOptions,
                    selectedOption = graphics,
                    onOptionSelected = { graphics = it }
                )
            }

            item {
                SelectorDropdownCard(
                    title = "Frame Rate Target (FPS)",
                    options = fpsOptions,
                    selectedOption = fpsLimit,
                    onOptionSelected = { fpsLimit = it }
                )
            }

            item {
                SelectorDropdownCard(
                    title = "Visual Color Style",
                    options = styleOptions,
                    selectedOption = stylePreset,
                    onOptionSelected = { stylePreset = it }
                )
            }

            // Boolean Switches List
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexDarkGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "⚡ Auxiliary Optimization Shaders",
                            color = NexCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Shadows switch
                        ToggleSwitchRow(
                            label = "Advance Shadow Rendering",
                            sub = "Increases detail, disables if lagging",
                            checked = isShadowEnabled,
                            onCheckedChange = { isShadowEnabled = it }
                        )

                        // Zero Lag switch
                        ToggleSwitchRow(
                            label = "Zero Lag Optimization Mode",
                            sub = "Minimizes framerate drops during team fights",
                            checked = isZeroLagEnabled,
                            onCheckedChange = { isZeroLagEnabled = it }
                        )

                        // Hardware Accel
                        ToggleSwitchRow(
                            label = "Hardware Vulkan Acceleration",
                            sub = "Leverages GPU cores directly",
                            checked = isHwAccelEnabled,
                            onCheckedChange = { isHwAccelEnabled = it }
                        )
                    }
                }
            }

            // Save Settings / Compile shaders button
            item {
                Button(
                    onClick = {
                        isCompilingGfx = true
                        scope.launch {
                            viewModel.updateGfxConfig(
                                resolution = resolution,
                                graphics = graphics,
                                fps = fpsLimit,
                                style = stylePreset,
                                shadow = isShadowEnabled,
                                zeroLag = isZeroLagEnabled,
                                hwAccel = isHwAccelEnabled
                            )
                            delay(3000) // Compilation time
                            isCompilingGfx = false
                            Toast.makeText(context, "GFX settings applied and saved!", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_gfx_settings"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NexCyan,
                        contentColor = NexBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Filled.AutoFixHigh, contentDescription = "Compile", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "APPLY & COMPILE SHADERS", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }

        // Overlay terminal overlay for compiling shader settings
        if (isCompilingGfx) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(color = NexCyan, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "COMPILING SHADER SOURCE CODE",
                        color = NexCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = NexDarkGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(0.5.dp, NexCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            LazyColumn {
                                item { Text("> Target graphics driver: Vulcan 1.3 aligned", color = Color.Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                                item { Text("> Custom Resolution profile: $resolution mapped", color = Color.Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                                item { Text("> Rendering target: $graphics engine initialized", color = Color.Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                                item { Text("> Limiting FPS constraint: $fpsLimit locked", color = Color.Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                                item { Text("> Injecting shadow vectors: $isShadowEnabled", color = Color.Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                                item { Text("> Hardware pipeline routing: VULKAN CORE 0", color = Color.Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                                item { Text("> Optimizing heap tables...", color = Color.Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                                item { Text("> COMPILATION COMPLETED. SUCCESS.", color = NexCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom dropdown selector component
@Composable
fun SelectorDropdownCard(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = NexDarkGray),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = NexLightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NexBlack)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Expand Options",
                    tint = NexCyan
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(NexDarkGray)
                    .border(0.5.dp, NexCyan.copy(alpha = 0.3f))
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, color = TextWhite, fontWeight = FontWeight.Bold) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Switch row layout
@Composable
fun ToggleSwitchRow(
    label: String,
    sub: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = sub, color = TextGray, fontSize = 10.sp)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NexBlack,
                checkedTrackColor = NexCyan,
                uncheckedThumbColor = NexLightGray,
                uncheckedTrackColor = NexBlack
            )
        )
    }
}

// ==========================================
// AI CHAT SCREEN (AI PERFORMANCE COACH)
// ==========================================
@Composable
fun AiCoachScreen(viewModel: EsportsViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val quickPerformanceQueries = listOf(
        "Suggest resolution settings for Call Of Duty on low-end device.",
        "How does Vulkan rendering help improve FPS in Teamfights?",
        "Roster analysis: captain: Faker, game: League of Legends.",
        "Predict Vanguard tournament outcome Sentinels vs Fnatic."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexBlack)
    ) {
        // AI Coach Active Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexDarkGray)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(NexCyan, NexElectricBlue)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = "AI Coach Banner logo",
                    tint = NexBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI Performance Coach",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isAiLoading) NexCyan else Color.Green, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAiLoading) "Compiling optimization analysis..." else "Locked & Listening",
                        color = if (isAiLoading) NexCyan else Color.Green,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.size == 1) {
                item {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "💡 Quick Query Suggestions:",
                            color = NexCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(quickPerformanceQueries) { query ->
                                Surface(
                                    color = NexDarkGray,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .border(0.5.dp, NexCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .clickable { viewModel.sendChatMessage(query) }
                                ) {
                                    Text(
                                        text = query,
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(messages) { msg ->
                ChatBubble(message = msg)
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(NexDarkGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = NexCyan,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 1.5.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reading performance compiler parameters...",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Text Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexDarkGray)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask gaming coach...", color = NexLightGray) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = NexCyan,
                    unfocusedBorderColor = NexLightGray.copy(alpha = 0.5f),
                    focusedContainerColor = NexBlack,
                    unfocusedContainerColor = NexBlack
                ),
                maxLines = 3,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendChatMessage(textInput)
                            textInput = ""
                            keyboardController?.hide()
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                },
                containerColor = NexCyan,
                contentColor = NexBlack,
                shape = CircleShape,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("chat_send_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send Coach Message",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColors = if (message.isUser) {
        Brush.linearGradient(listOf(NexElectricBlue, NexElectricBlue.copy(alpha = 0.8f)))
    } else {
        Brush.linearGradient(listOf(NexCardGray, NexDarkGray))
    }
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, top = 4.dp)
                        .size(24.dp)
                        .background(NexCyan.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = "AI icon",
                        tint = NexCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(bgColors, shape = bubbleShape)
                    .border(
                        width = 0.5.dp,
                        color = if (message.isUser) NexCyan.copy(alpha = 0.3f) else NexElectricBlue.copy(alpha = 0.15f),
                        shape = bubbleShape
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ==========================================
// ESPORTS HUB SCREEN
// ==========================================
@Composable
fun EsportsHubScreen(viewModel: EsportsViewModel, navController: NavController) {
    val teams by viewModel.teams.collectAsStateWithLifecycle()
    val joinedTournaments by viewModel.joinedTournaments.collectAsStateWithLifecycle()
    var showCreateTeamDialog by remember { mutableStateOf(false) }
    var showJoinTournamentDialog by remember { mutableStateOf(false) }
    var selectedTournamentForJoin by remember { mutableStateOf<EsportsTournament?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Banner Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_nex_arena_banner),
                        contentDescription = "Esports Arena Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, NexBlack.copy(alpha = 0.6f), NexBlack)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NexCyan,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "ARENA LEAGUE PORTAL",
                                color = NexBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Ultimate Esports Scrims",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    }
                }
            }

            // Teams Registration Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🛡️ Team Rosters",
                            color = NexCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage your tournament squad list",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showCreateTeamDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NexCyan, contentColor = NexBlack),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("add_team_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Roster", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Register Squad", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Teams list items
            if (teams.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NexDarkGray)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No squads registered. Click 'Register Squad' to configure your tournament roster!",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(teams) { team ->
                    TeamItemCard(team = team, onDeleteClick = {
                        viewModel.deleteTeam(team.id)
                        Toast.makeText(context, "Removed Team ${team.name}", Toast.LENGTH_SHORT).show()
                    })
                }
            }

            // Active Matches Section
            item {
                Text(
                    text = "🔥 Live Tournament Matchups",
                    color = NexCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.matches) { match ->
                        MatchCardItem(match = match, onPredictClick = {
                            viewModel.predictMatchOutcome(match)
                            navController.navigate(Routes.AI_COACH)
                        })
                    }
                }
            }

            // Tournaments list
            item {
                Text(
                    text = "🏆 Active Arena Tournaments",
                    color = NexCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            items(viewModel.tournaments) { tournament ->
                TournamentItemRow(
                    tournament = tournament,
                    onJoinClick = {
                        if (teams.isEmpty()) {
                            Toast.makeText(context, "Register a Squad first on Team Roster panel!", Toast.LENGTH_LONG).show()
                        } else {
                            selectedTournamentForJoin = tournament
                            showJoinTournamentDialog = true
                        }
                    }
                )
            }
        }

        // Create Team Dialog
        if (showCreateTeamDialog) {
            CreateTeamDialog(
                onDismiss = { showCreateTeamDialog = false },
                onConfirm = { name, game, captain, members ->
                    viewModel.createTeam(name, game, captain, members)
                    showCreateTeamDialog = false
                    Toast.makeText(context, "Esports Team $name registered successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Join Tournament Dialog
        if (showJoinTournamentDialog && selectedTournamentForJoin != null) {
            JoinTournamentDialog(
                tournament = selectedTournamentForJoin!!,
                teams = teams,
                onDismiss = { showJoinTournamentDialog = false },
                onConfirmJoin = { selectedTeam ->
                    viewModel.joinTournament(
                        tournamentTitle = selectedTournamentForJoin!!.title,
                        game = selectedTournamentForJoin!!.game,
                        teamId = selectedTeam.id,
                        teamName = selectedTeam.name
                    )
                    showJoinTournamentDialog = false
                    Toast.makeText(context, "Registered ${selectedTeam.name} in ${selectedTournamentForJoin!!.title}!", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

@Composable
fun TeamItemCard(team: Team, onDeleteClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexDarkGray),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = team.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Game: ${team.game} | Captain: ${team.captain}", color = NexCyan, fontSize = 11.sp)
                Text(text = "Roster: ${team.members}", color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete team", tint = Color.Red, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun MatchCardItem(match: EsportsMatch, onPredictClick: () -> Unit) {
    val isLive = match.status == "Live"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NexDarkGray),
        modifier = Modifier
            .width(240.dp)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    if (isLive) listOf(NexCyan.copy(alpha = 0.4f), Color.Transparent)
                    else listOf(Color.Transparent, Color.Transparent)
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = match.game, color = NexCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                if (isLive) {
                    Text(text = "LIVE", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text(text = match.time, color = TextGray, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = match.teamA, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = if (!isLive && match.status != "Finished") "-" else match.scoreA.toString(), color = TextWhite, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = match.teamB, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = if (!isLive && match.status != "Finished") "-" else match.scoreB.toString(), color = TextWhite, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPredictClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NexElectricBlue, contentColor = TextWhite),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.QueryStats, contentDescription = "AI Stats", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Analyze with AI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TournamentItemRow(tournament: EsportsTournament, onJoinClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexDarkGray),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tournament.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${tournament.game} | Pool: ${tournament.prizePool}", color = NexCyan, fontSize = 11.sp)
                Text(text = "Schedule: ${tournament.date}", color = TextGray, fontSize = 10.sp)
            }

            if (tournament.status != "Completed") {
                Button(
                    onClick = onJoinClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NexCyan, contentColor = NexBlack),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(text = "Join Scrim", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun CreateTeamDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var game by remember { mutableStateOf("Valorant") }
    var captain by remember { mutableStateOf("") }
    var members by remember { mutableStateOf("") }

    val games = listOf("Valorant", "Apex Legends", "CS2", "League of Legends", "Dota 2")
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexDarkGray,
        title = {
            Text(text = "Register Arena Roster", color = NexCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Squad Team Name", color = NexLightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NexCyan,
                        unfocusedBorderColor = NexLightGray
                    ),
                    modifier = Modifier.testTag("team_name_input")
                )

                Box {
                    OutlinedTextField(
                        value = game,
                        onValueChange = {},
                        label = { Text("Game Title Selection", color = NexLightGray) },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = NexCyan,
                            unfocusedBorderColor = NexLightGray
                        ),
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Expand", tint = NexCyan)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(NexDarkGray)
                    ) {
                        games.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(text = g, color = TextWhite) },
                                onClick = {
                                    game = g
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = captain,
                    onValueChange = { captain = it },
                    label = { Text("Roster Team Captain", color = NexLightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NexCyan,
                        unfocusedBorderColor = NexLightGray
                    )
                )

                OutlinedTextField(
                    value = members,
                    onValueChange = { members = it },
                    label = { Text("Members (comma-separated)", color = NexLightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NexCyan,
                        unfocusedBorderColor = NexLightGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && captain.isNotBlank()) {
                        onConfirm(name, game, captain, members)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexCyan, contentColor = NexBlack),
                enabled = name.isNotBlank() && captain.isNotBlank()
            ) {
                Text("Lock Team")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
fun JoinTournamentDialog(
    tournament: EsportsTournament,
    teams: List<Team>,
    onDismiss: () -> Unit,
    onConfirmJoin: (Team) -> Unit
) {
    var selectedTeamIndex by remember { mutableStateOf(0) }
    val eligibleTeams = teams.filter { it.game.equals(tournament.game, ignoreCase = true) }
    val teamsToShow = if (eligibleTeams.isNotEmpty()) eligibleTeams else teams

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexDarkGray,
        title = {
            Text(
                text = "Join Arena League",
                color = NexCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Tournament: ${tournament.title}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Required Game: ${tournament.game}",
                    color = TextGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Select your team:",
                    color = TextWhite,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    items(teamsToShow.size) { index ->
                        val team = teamsToShow[index]
                        val isSelected = index == selectedTeamIndex

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NexElectricBlue.copy(alpha = 0.3f) else NexCardGray)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NexCyan else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTeamIndex = index }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = team.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Captain: ${team.captain} • ${team.game}", color = TextGray, fontSize = 11.sp)
                            }
                            if (isSelected) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = "Selected", tint = NexCyan)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (teamsToShow.isNotEmpty()) {
                        onConfirmJoin(teamsToShow[selectedTeamIndex])
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexCyan, contentColor = NexBlack),
                enabled = teamsToShow.isNotEmpty()
            ) {
                Text("Confirm Registration")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

