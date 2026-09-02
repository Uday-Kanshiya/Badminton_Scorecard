package com.badminton.scorecard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.badminton.scorecard.feature.live_scoreboard.presentation.LiveMatchScreen
import com.badminton.scorecard.feature.match_history.presentation.MatchHistoryScreen
import com.badminton.scorecard.feature.match_setup.presentation.MatchSetupScreen
import com.badminton.scorecard.feature.match_summary.presentation.MatchSummaryScreen
import com.badminton.scorecard.feature.player.presentation.PlayerListScreen
import com.badminton.scorecard.feature.player.presentation.PlayerProfileScreen
import com.badminton.scorecard.feature.statistics.presentation.PartnershipAnalysisScreen
import com.badminton.scorecard.feature.statistics.presentation.StatsDashboardScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        // Home screen
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToNewMatch = { navController.navigate(NewMatchRoute) },
                onNavigateToPlayers = { navController.navigate(PlayersRoute) },
                onNavigateToHistory = { navController.navigate(HistoryRoute) },
                onNavigateToStats = { navController.navigate(StatsRoute) },
                onNavigateToMatchDetail = { matchId -> navController.navigate(MatchSummaryRoute(matchId)) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) }
            )
        }
        
        // Players
        composable<PlayersRoute> {
            PlayerListScreen(
                onNavigateToProfile = { playerId ->
                    navController.navigate(PlayerProfileRoute(playerId))
                }
            )
        }
        
        composable<PlayerProfileRoute> {
            PlayerProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Match flow
        composable<NewMatchRoute> {
            MatchSetupScreen(
                onNavigateToLiveMatch = { matchId ->
                    navController.navigate(LiveMatchRoute(matchId)) {
                        popUpTo(NewMatchRoute) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<LiveMatchRoute> {
            LiveMatchScreen(
                onNavigateToSummary = { matchId ->
                    navController.navigate(MatchSummaryRoute(matchId)) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<MatchSummaryRoute> {
            MatchSummaryScreen(
                onNavigateHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(HomeRoute) { inclusive = true }
                    }
                }
            )
        }
        
        // History
        composable<HistoryRoute> {
            MatchHistoryScreen(
                onNavigateToMatchDetail = { matchId ->
                    navController.navigate(MatchSummaryRoute(matchId))
                }
            )
        }
        
        // Stats
        composable<StatsRoute> {
            StatsDashboardScreen(
                onNavigateToPartnerships = {
                    navController.navigate(PartnershipAnalysisRoute)
                }
            )
        }
        
        composable<PartnershipAnalysisRoute> {
            PartnershipAnalysisScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            com.badminton.scorecard.feature.settings.presentation.SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
