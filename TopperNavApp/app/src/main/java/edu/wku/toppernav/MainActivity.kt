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

// This file wires together navigation (Compose NavHost), CSV import, search, and basic GPS.
// Key flow: SearchScreen -> user picks a 'BUILDING ROOM' string -> Navigate screen
// Navigate parses that string, looks up coordinates, asks for location permission, then computes distance/ETA.

// Helper to parse a destination string like "SNELL HALL B104" -> ("SNELL HALL", "B104")
private fun parseDestination(raw: String): Pair<String, String>? {
    val tokens = raw.trim().split(Regex("\\s+")) // split on one or more whitespace
    if (tokens.size < 2) return null
    val room = tokens.last()
    val building = tokens.dropLast(1).joinToString(" ")
    return building to room
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ToppernavApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToppernavApp() {
    ToppernavTheme {
        val nav = rememberNavController()
        val context = LocalContext.current
        val db = remember { TopperNavDatabase.getInstance(context) }

        // First run: import CSV if DB empty. Runs off main thread so UI stays snappy.
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) { CsvRoomImporter(context, db).importIfEmpty() }
        }

        // Construct repository + use case + view model (simple DI by hand).
        val searchVm: SearchViewModel = viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = NavigationRepositoryImpl(db.roomDao())
                val useCase = SearchRoomsUseCase(repo)
                @Suppress("UNCHECKED_CAST") return SearchViewModel(useCase) as T
            }
        })

        // NavigationViewModel holds live location + destination + computed metrics.
        val app = (context.applicationContext as Application)
        val navVm: NavigationViewModel = viewModel(factory = viewModelFactory { initializer { NavigationViewModel(app) } })

        // Simple UI-level state (greeting name, history, selected destination string).
        var displayName by rememberSaveable { mutableStateOf("") }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        val history = remember { mutableStateListOf<String>() }
        var selectedDestination by rememberSaveable { mutableStateOf<String?>(null) }

        val tabs = listOf(
            Tab("search", "Search", Icons.Filled.Search),
            Tab("navigate", "Navigate", Icons.Filled.Navigation),
            Tab("history", "History", Icons.Filled.History)
        )

        val backStackEntry by nav.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            topBar = {
                when (currentRoute) {
                    "settings" -> {
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
                        val title = selectedDestination?.takeIf { it.isNotBlank() } ?: "Navigate"
                        CenterAlignedTopAppBar(
                            title = { Text(title) },
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
                    else -> {
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
                if (currentRoute != "settings") {
                    NavigationBar {
                        tabs.forEach { tab ->
                            val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
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
            NavHost(navController = nav, startDestination = "search", modifier = Modifier.padding(inner)) {
                composable("search") {
                    val results by searchVm.results.collectAsState()
                    val loading by searchVm.loading.collectAsState()
                    // SearchScreen is intentionally dumb: it just emits queries and shows strings.
                    SearchScreen(
                        query = searchQuery,
                        loading = loading,
                        results = results,
                        onQueryChange = { q ->
                            searchQuery = q
                            // Simple throttle rule: only search for length >= 2.
                            if (q.length >= 2) {
                                Log.d("Perf", "Search start ns=${System.nanoTime()} q='$q'")
                                searchVm.search(q)
                            }
                        },
                        onEnter = { text ->
                            val trimmed = text.trim()
                            if (trimmed.isNotEmpty()) {
                                // Maintain a simple in-memory history (most recent at top).
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

                    // Derived UI strings.
                    val etaText = state.etaMinutes?.let { "$it min" } ?: "—"
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
                        etaText = etaText,
                        travelTimeText = etaText,
                        steps = emptyList(),
                        statusLine = statusLine,
                        bearingDeg = state.bearingDeg?.toFloat(),
                        floorAdvice = state.floorAdvice,
                        debugLines = dbg
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
