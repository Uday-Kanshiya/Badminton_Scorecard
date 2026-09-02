package com.badminton.scorecard.navigation

import kotlinx.serialization.Serializable

// Bottom nav destinations
@Serializable object HomeRoute
@Serializable object PlayersRoute  
@Serializable object NewMatchRoute
@Serializable object HistoryRoute
@Serializable object StatsRoute

// Detail destinations
@Serializable data class PlayerProfileRoute(val playerId: Long)
@Serializable data class LiveMatchRoute(val matchId: Long)
@Serializable data class MatchSummaryRoute(val matchId: Long)
@Serializable object PartnershipAnalysisRoute
@Serializable object SettingsRoute
