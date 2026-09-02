package com.badminton.scorecard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.rememberNavController
import com.badminton.scorecard.core.designsystem.theme.BadmintonScorecardTheme
import com.badminton.scorecard.core.preferences.ThemeMode
import com.badminton.scorecard.core.preferences.ThemePreferences
import com.badminton.scorecard.navigation.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentThemeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            BadmintonScorecardTheme(themeMode = currentThemeMode) {
                val navController = rememberNavController()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route

                // Hide bottom bar on specific screens
                val showBottomBar = currentRoute != null && 
                        !currentRoute.contains(LiveMatchRoute::class.qualifiedName ?: "") &&
                        !currentRoute.contains(MatchSummaryRoute::class.qualifiedName ?: "") &&
                        !currentRoute.contains("SettingsRoute")

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                val items = listOf(
                                    BottomNavItem("Home", Icons.Default.Home, HomeRoute),
                                    BottomNavItem("Players", Icons.Default.Person, PlayersRoute),
                                    BottomNavItem("New Match", Icons.Default.Add, NewMatchRoute),
                                    BottomNavItem("History", Icons.AutoMirrored.Default.List, HistoryRoute),
                                    BottomNavItem("Stats", Icons.Default.Star, StatsRoute)
                                )
                                
                                items.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any { 
                                        it.route?.contains(item.route::class.qualifiedName ?: "") == true ||
                                        (item.route == HomeRoute && it.route?.contains("HomeRoute") == true) ||
                                        (item.route == PlayersRoute && it.route?.contains("PlayersRoute") == true) ||
                                        (item.route == NewMatchRoute && it.route?.contains("NewMatchRoute") == true) ||
                                        (item.route == HistoryRoute && it.route?.contains("HistoryRoute") == true) ||
                                        (item.route == StatsRoute && it.route?.contains("StatsRoute") == true)
                                    } == true

                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any
)
