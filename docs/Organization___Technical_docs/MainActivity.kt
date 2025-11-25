package edu.wku.toppernav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.wku.toppernav.data.importcsv.CsvRoomImporter
import edu.wku.toppernav.data.local.db.TopperNavDatabase
import edu.wku.toppernav.data.repository.NavigationRepositoryImpl
import edu.wku.toppernav.domain.usecase.SearchRoomsUseCase
import edu.wku.toppernav.ui.screens.HistoryScreen
import edu.wku.toppernav.ui.screens.NavigationScreen
import edu.wku.toppernav.ui.screens.SearchScreen
import edu.wku.toppernav.ui.screens.SettingsScreen
import edu.wku.toppernav.viewmodel.SearchViewModel
import edu.wku.toppernav.ui.theme.ToppernavTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import edu.wku.toppernav.viewmodel.NavigationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.runtime.rememberCoroutineScope
import android.app.Application
import kotlinx.coroutines.launch
import android.util.Log

// MainActivity - main entry point for the app
// Sets up the screens (Search, Navigate, History, Settings) and wires them together
// Also handles the CSV import on first launch and sets up the database

// Quick helper to split "SNELL HALL B104" into building="SNELL HALL" and room="B104"
// Just splits on spaces and takes the last token as the room number
private fun parseDestination(raw: String): Pair<String, String>? {
    val tokens = raw.trim().split(Regex("\\s+"))
    if (tokens.size < 2) return null
    val room = tokens.last()
    val building = tokens.dropLast(1).joinToString(" ")
    return building to room
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // modern Android edge-to-edge display
        setContent { ToppernavApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToppernavApp() {
    ToppernavTheme {
        val nav = rememberNavController() // handles switching between screens
        val context = LocalContext.current
        val db = remember { TopperNavDatabase.getInstance(context) }

        // Import the CSV (toppernav_export.csv) on first launch if the database is empty
        // Runs on a background thread so it doesn't freeze the UI
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) { CsvRoomImporter(context, db).importIfEmpty() }
        }

        // Set up SearchViewModel - handles searching for rooms in the database
        // We're doing simple dependency injection by hand here (no Dagger/Hilt)
        val searchVm: SearchViewModel = viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = NavigationRepositoryImpl(db.roomDao())
                val useCase = SearchRoomsUseCase(repo)
                @Suppress("UNCHECKED_CAST") return SearchViewModel(useCase) as T
            }
        })

        // NavigationViewModel handles GPS location tracking and distance/ETA calculations
        val app = (context.applicationContext as Application)
        val navVm: NavigationViewModel = viewModel(factory = viewModelFactory { initializer { NavigationViewModel(app) } })

        // Simple state for the UI - user name, search query, history, and selected destination
        var displayName by rememberSaveable { mutableStateOf("") }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        val history = remember { mutableStateListOf<String>() } // recent searches
        var selectedDestination by rememberSaveable { mutableStateOf<String?>(null) }

        // Bottom nav tabs: Search, Navigate, History
        val tabs = listOf(
            Tab("search", "Search", Icons.Filled.Search),
            Tab("navigate", "Navigate", Icons.Filled.Navigation),
            Tab("history", "History", Icons.Filled.History)
        )

        val backStackEntry by nav.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            topBar = {
                // Different top bars for different screens
                when (currentRoute) {
                    "settings" -> {
                        // Settings screen gets a back button
                        CenterAlignedTopAppBar(
                            title = { Text("Settings") },
                            navigationIcon = {
                                IconButton(onClick = { nav.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    "navigate" -> {
                        // Navigate screen shows the destination in the title and a back button to return to Search
                        val title = selectedDestination?.takeIf { it.isNotBlank() } ?: "Navigate"
                        CenterAlignedTopAppBar(
                            title = { Text(title) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    // Go back to Search when user taps the back arrow
                                    nav.navigate("search") {
                                        popUpTo(nav.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    else -> {
                        // Main screens (Search/History) show a greeting and a settings button
                        val greet = if (displayName.isBlank()) "Hilltopper" else displayName.trim()
                        CenterAlignedTopAppBar(
                            title = { Text("Hi $greet") },
                            actions = {
                                IconButton(onClick = { nav.navigate("settings") }) {
                                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            },
            bottomBar = {
                // Show bottom nav bar on all screens except Settings
                if (currentRoute != "settings") {
                    NavigationBar {
                        tabs.forEach { tab ->
                            val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    // Only navigate if we're not already on that tab
                                    if (!selected) nav.navigate(tab.route) {
                                        popUpTo(nav.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        ) { inner ->
            // The actual screen content - NavHost handles switching between screens
            NavHost(navController = nav, startDestination = "search", modifier = Modifier.padding(inner)) {
                composable("search") {
                    val results by searchVm.results.collectAsState()
                    val loading by searchVm.loading.collectAsState()

                    // SearchScreen just shows a text field and displays results
                    // Triggers search when user types 2+ characters
                    SearchScreen(
                        query = searchQuery,
                        loading = loading,
                        results = results,
                        onQueryChange = { q ->
                            searchQuery = q
                            // Only search if user typed at least 2 characters (reduces spam)
                            if (q.length >= 2) {
                                Log.d("Perf", "Search start ns=${System.nanoTime()} q='$q'")
                                searchVm.search(q)
                            }
                        },
                        onEnter = { text ->
                            // User selected a destination from the results
                            val trimmed = text.trim()
                            if (trimmed.isNotEmpty()) {
                                // Add to history (most recent at top)
                                history.remove(trimmed)
                                history.add(0, trimmed)
                                selectedDestination = trimmed
                                nav.navigate("navigate")
                            }
                        }
                    )
                }
                composable("navigate") {
                    val ctx = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val state by navVm.state.collectAsState()

                    // Auto-update the displayed ETA every 60 seconds
                    // Even if user doesn't move, the clock time changes so ETA needs to refresh
                    var tickCounter by remember { mutableStateOf(0) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            kotlinx.coroutines.delay(60_000L) // 60 seconds
                            tickCounter++ // Force recomposition so ETA recalculates
                        }
                    }

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { result ->
                            val granted = (result[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                                    (result[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true)
                            navVm.setPermission(granted)
                            Log.d("NAV", "Permission result granted=$granted")
                        }
                    )

                    LaunchedEffect(selectedDestination) {
                        // Auto grant logic for mock demo: if mock enabled we treat as permitted to seed location
                        if (edu.wku.toppernav.core.AppConfig.mockLocationEnabled && !state.hasPermission) {
                            Log.d("NAV", "Mock location enabled; forcing permission true for demo")
                            navVm.setPermission(true)
                        }

                        val fine = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarse = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (!fine && !coarse && !edu.wku.toppernav.core.AppConfig.mockLocationEnabled) {
                            Log.d("NAV", "Requesting runtime location permission")
                            launcher.launch(arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        } else {
                            navVm.setPermission(true)
                        }

                        val raw = selectedDestination
                        if (!raw.isNullOrBlank()) {
                            val parsed = parseDestination(raw)
                            if (parsed == null) {
                                Log.w("NAV", "Could not parse destination '$raw'")
                            } else {
                                val (building, room) = parsed
                                Log.d("NAV", "Parsed building='$building' room='$room'")
                                scope.launch(Dispatchers.IO) {
                                    val entity = db.roomDao().findByBuildingAndRoom(building.uppercase(), room)
                                    if (entity == null) {
                                        Log.w("NAV", "No match for building='${building.uppercase()}' room='$room'")
                                    } else {
                                        navVm.setDestination(entity.lat, entity.lng, entity.altM, entity.floor?.toInt())
                                        Log.d("NAV", "Destination set lat=${entity.lat} lng=${entity.lng}")
                                    }
                                }
                            }
                        }
                    }

                    // Figure out what to show in the UI based on the current state
                    // ETA = actual arrival time (current time + travel time)
                    // Travel time = just the duration
                    // tickCounter forces recalculation every 60 seconds
                    val travelTimeText = state.etaMinutes?.let { "$it min" } ?: "—"
                    val etaText = state.etaMinutes?.let { minutes ->
                        // Use tickCounter in calculation so this recomputes every 60 seconds
                        @Suppress("UNUSED_EXPRESSION") tickCounter
                        val now = java.util.Calendar.getInstance()
                        now.add(java.util.Calendar.MINUTE, minutes)
                        val hour = now.get(java.util.Calendar.HOUR)
                        val displayHour = if (hour == 0) 12 else hour
                        val minute = now.get(java.util.Calendar.MINUTE)
                        val amPm = if (now.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
                        "$displayHour:${"%02d".format(minute)} $amPm"
                    } ?: "—"
                    val statusLine = when {
                        state.status.isNotBlank() -> state.status
                        selectedDestination != null && state.distanceMeters == null -> if (state.hasPermission) "Waiting for GPS fix..." else "Grant location permission"
                        else -> null
                    }

                    val dbg = listOfNotNull(
                        "perm=${state.hasPermission}",
                        state.userLat?.let { "user=(${"%.5f".format(it)}, ${"%.5f".format(state.userLng ?: 0.0)})" },
                        state.destLat?.let { "dest=(${"%.5f".format(it)}, ${"%.5f".format(state.destLng ?: 0.0)})" },
                        state.distanceMeters?.let { "dist=${"%.1f".format(it)}m" },
                        state.bearingDeg?.let { "bearing=${"%.0f".format(it)}°" },
                        state.etaMinutes?.let { "eta=${it}m" },
                        state.floorAdvice
                    )

                    NavigationScreen(
                        destination = selectedDestination,
                        etaText = etaText,              // Arrival time like "3:52 PM"
                        travelTimeText = travelTimeText, // Duration like "8 min"
                        steps = emptyList(),
                        statusLine = statusLine,
                        bearingDeg = state.bearingDeg?.toFloat(),
                        floorAdvice = state.floorAdvice,
                        debugLines = dbg,
                        providerInfo = state.provider?.let { p ->
                            val acc = state.accuracyMeters?.let { " acc=${"%.1f".format(it)}m" } ?: ""
                            "$p$acc"
                        },
                        onRecenter = {
                            // User tapped Recenter button - force immediate GPS refresh
                            navVm.forceRefresh()
                        }
                    )
                }
                composable("history") {
                    HistoryScreen(
                        items = history,
                        onSelect = { item ->
                            searchQuery = item
                            nav.navigate("search")
                        }
                    )
                }
                composable("settings") {
                    SettingsScreen(name = displayName, onNameChange = { displayName = it })
                }
            }
        }
    }
}

data class Tab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
