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
import edu.wku.toppernav.ui.screens.HistoryScreen
import edu.wku.toppernav.ui.screens.NavigationScreen
import edu.wku.toppernav.ui.screens.SearchScreen
import edu.wku.toppernav.ui.screens.SettingsScreen
import edu.wku.toppernav.ui.theme.ToppernavTheme

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

        // Shared app state (UI-only for sprint)
        var displayName by rememberSaveable { mutableStateOf("") } // blank -> "Hilltopper"
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
            NavHost(
                navController = nav,
                startDestination = "search",
                modifier = Modifier.padding(inner)
            ) {
                composable("search") {
                    SearchScreen(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onEnter = { text ->
                            val trimmed = text.trim()
                            if (trimmed.isNotEmpty()) {
                                history.remove(trimmed)
                                history.add(0, trimmed)
                                selectedDestination = trimmed
                                nav.navigate("navigate")
                            }
                        }
                    )
                }
                composable("navigate") {
                    NavigationScreen(
                        destination = selectedDestination,
                        etaText = "—",
                        travelTimeText = "—",
                        steps = emptyList()
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
                    SettingsScreen(
                        name = displayName,
                        onNameChange = { displayName = it }
                    )
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
