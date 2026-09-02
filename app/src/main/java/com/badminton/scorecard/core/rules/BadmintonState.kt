package com.badminton.scorecard.core.rules

/**
 * Represents basic player information.
 *
 * @property id Unique identifier for the player.
 * @property name Display name of the player.
 */
data class PlayerInfo(
    val id: Long,
    val name: String
)

/**
 * Represents the state of a team on the court.
 *
 * @property team The team identifier (Team A or Team B).
 * @property player1 The player currently positioned in the RIGHT court (or main player in singles).
 * @property player2 The player currently positioned in the LEFT court (null for singles).
 * @property score Current score of the team in the current set.
 */
data class TeamCourtState(
    val team: TeamSide,
    val player1: PlayerInfo,  // Current right-court player
    val player2: PlayerInfo?, // Current left-court player (null for singles)
    val score: Int = 0
) {
    /**
     * Gets the player currently positioned at the specified court side.
     * In singles, player1 covers the whole court so it always returns player1.
     */
    fun playerAt(court: CourtSide): PlayerInfo = when (court) {
        CourtSide.RIGHT -> player1
        CourtSide.LEFT -> player2 ?: player1
    }
    
    /**
     * Swaps the positions of the players (left goes right, right goes left).
     * Used in doubles when the serving team scores a point.
     */
    fun swapPositions(): TeamCourtState = copy(
        player1 = player2 ?: player1,
        player2 = if (player2 != null) player1 else null
    )
}

/**
 * Represents the final score of a single set.
 */
data class SetScore(
    val setNumber: Int,
    val teamAScore: Int,
    val teamBScore: Int,
    val winner: TeamSide? = null
)

/**
 * Represents the live state of an ongoing badminton match.
 * Contains all necessary information to render the scorecard and apply BWF rules.
 */
data class BadmintonLiveState(
    val matchType: MatchType,
    val targetPoints: Int = 21,
    val bestOfSets: Int = 1,
    val skunkRuleEnabled: Boolean = false,
    val currentSetNumber: Int = 1,
    val teamA: TeamCourtState,
    val teamB: TeamCourtState,
    val servingTeam: TeamSide,
    val serverPlayer: PlayerInfo,
    val receiverPlayer: PlayerInfo,
    val serverCourt: CourtSide = CourtSide.RIGHT,
    val completedSets: List<SetScore> = emptyList(),
    val setsWonA: Int = 0,
    val setsWonB: Int = 0,
    val isSetOver: Boolean = false,
    val isMatchOver: Boolean = false,
    val matchWinner: TeamSide? = null,
    val rallyNumber: Int = 0,
    val isSidesSwapped: Boolean = false
) {
    /**
     * The score of the current ongoing set.
     */
    val currentSetScore: SetScore get() = SetScore(
        setNumber = currentSetNumber,
        teamAScore = teamA.score,
        teamBScore = teamB.score
    )
    
    /**
     * Retrieves the state of the specified team.
     */
    fun getTeam(side: TeamSide): TeamCourtState = when (side) {
        TeamSide.TEAM_A -> teamA
        TeamSide.TEAM_B -> teamB
    }
    
    /**
     * Checks if the match is currently in a deuce state.
     * Deuce occurs when both teams reach (targetPoints - 1) and have equal scores.
     */
    val isDeuce: Boolean get() {
        val a = teamA.score
        val b = teamB.score
        return a >= targetPoints - 1 && b >= targetPoints - 1 && a == b
    }

    /**
     * Generates standard BWF umpire call text matching professional tournament displays.
     */
    val umpireCall: String get() {
        val servingScore = if (servingTeam == TeamSide.TEAM_A) teamA.score else teamB.score
        val receivingScore = if (servingTeam == TeamSide.TEAM_A) teamB.score else teamA.score
        return when {
            isMatchOver -> "${if (matchWinner == TeamSide.TEAM_A) "Team A" else "Team B"} wins the match!"
            isSetOver -> "End of set $currentSetNumber."
            servingScore == 0 && receivingScore == 0 -> "${serverPlayer.name} to serve. Love all. Play."
            isDeuce -> "${serverPlayer.name} to serve. Deuce. $servingScore all."
            servingScore == receivingScore -> "${serverPlayer.name} to serve. $servingScore all."
            servingScore >= targetPoints - 1 && servingScore > receivingScore -> "${serverPlayer.name} to serve. Game point."
            else -> "${serverPlayer.name} to serve. $servingScore - $receivingScore."
        }
    }
}
