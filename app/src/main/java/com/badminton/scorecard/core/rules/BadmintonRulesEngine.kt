package com.badminton.scorecard.core.rules

/**
 * The core business logic engine for a Badminton match.
 * Follows BWF (Badminton World Federation) rules for scoring, serving rotation, and court positioning.
 */
class BadmintonRulesEngine {
    
    private val stateHistory = ArrayDeque<BadmintonLiveState>()
    private val redoHistory = ArrayDeque<BadmintonLiveState>()
    
    /**
     * Creates the initial state for a new match.
     * 
     * @param matchType Singles or Doubles.
     * @param targetPoints Points required to win a set (usually 21).
     * @param bestOfSets Number of sets to play (usually 1, 3, or 5).
     * @param teamAPlayers List of players for Team A (1 for singles, 2 for doubles).
     * @param teamBPlayers List of players for Team B (1 for singles, 2 for doubles).
     * @param firstServingTeam The team that won the toss and elected to serve first.
     * @return The initial live state of the match.
     */
    fun createInitialState(
        matchType: MatchType,
        targetPoints: Int,
        bestOfSets: Int,
        teamAPlayers: List<PlayerInfo>,  // 1 for singles, 2 for doubles
        teamBPlayers: List<PlayerInfo>,
        firstServingTeam: TeamSide,
        serviceRotationEnabled: Boolean = true
    ): BadmintonLiveState {
        val teamA = TeamCourtState(
            team = TeamSide.TEAM_A,
            player1 = teamAPlayers[0],  // starts on RIGHT court
            player2 = teamAPlayers.getOrNull(1), // LEFT court (doubles)
            score = 0
        )
        val teamB = TeamCourtState(
            team = TeamSide.TEAM_B,
            player1 = teamBPlayers[0],
            player2 = teamBPlayers.getOrNull(1),
            score = 0
        )
        
        // Skunk rule: only for doubles + 21 points
        val skunkEnabled = matchType == MatchType.DOUBLES && targetPoints == 21
        
        val servingTeam = if (firstServingTeam == TeamSide.TEAM_A) teamA else teamB
        val receivingTeam = if (firstServingTeam == TeamSide.TEAM_A) teamB else teamA
        
        return BadmintonLiveState(
            matchType = matchType,
            targetPoints = targetPoints,
            bestOfSets = bestOfSets,
            skunkRuleEnabled = skunkEnabled,
            teamA = teamA,
            teamB = teamB,
            servingTeam = firstServingTeam,
            serverPlayer = servingTeam.player1, // Right court player serves first (score is 0 = even)
            receiverPlayer = receivingTeam.player1, // Right court player receives
            serverCourt = CourtSide.RIGHT,
            isServiceRotationEnabled = serviceRotationEnabled
        )
    }
    
    /**
     * Records a point scored by the given team.
     * Returns the new state after processing the point.
     * 
     * BWF Rules:
     * - Serving side scores → same server, serving pair swap courts (doubles), serve again
     * - Receiving side scores → service changes, nobody swaps courts
     * - Even score → serve from RIGHT, Odd score → serve from LEFT
     * - No deuce cap: play until 2-point lead
     * - Skunk rule (doubles + 21pt): 7-0 = instant victory
     * 
     * @param currentState The current match state.
     * @param scoringTeam The team that scored the point.
     * @return The updated match state.
     */
    fun recordPoint(
        currentState: BadmintonLiveState,
        scoringTeam: TeamSide
    ): BadmintonLiveState {
        if (currentState.isMatchOver || currentState.isSetOver) return currentState
        
        // Save state for undo
        stateHistory.addLast(currentState)
        redoHistory.clear()
        
        val isTeamAScoring = scoringTeam == TeamSide.TEAM_A
        val newTeamAScore = currentState.teamA.score + if (isTeamAScoring) 1 else 0
        val newTeamBScore = currentState.teamB.score + if (!isTeamAScoring) 1 else 0
        val newRallyNumber = currentState.rallyNumber + 1
        
        // Check skunk rule: 7-0 in doubles + 21pt
        val isSkunk = currentState.skunkRuleEnabled && (
            (newTeamAScore == 7 && newTeamBScore == 0) ||
            (newTeamBScore == 7 && newTeamAScore == 0)
        )
        
        // Check set win: score >= target AND lead by 2 (NO CAP)
        val leadingScore = maxOf(newTeamAScore, newTeamBScore)
        val trailingScore = minOf(newTeamAScore, newTeamBScore)
        val isSetWon = isSkunk || (leadingScore >= currentState.targetPoints && (leadingScore - trailingScore) >= 2)
        
        val setWinner = if (isSetWon) scoringTeam else null
        
        // Process serve rotation
        val newTeamA: TeamCourtState
        val newTeamB: TeamCourtState
        val newServingTeam: TeamSide
        val newServerPlayer: PlayerInfo
        val newReceiverPlayer: PlayerInfo
        val newServerCourt: CourtSide
        
        val servingTeamScored = scoringTeam == currentState.servingTeam
        
        if (!currentState.isServiceRotationEnabled) {
            // No rotation mode: just update scores, toggle serve if needed
            newTeamA = currentState.teamA.copy(score = newTeamAScore)
            newTeamB = currentState.teamB.copy(score = newTeamBScore)
            
            if (servingTeamScored) {
                newServingTeam = currentState.servingTeam
            } else {
                newServingTeam = scoringTeam
            }
            
            // Use player1 as nominal server/receiver
            val servingTeamState = if (newServingTeam == TeamSide.TEAM_A) newTeamA else newTeamB
            val receivingTeamState = if (newServingTeam == TeamSide.TEAM_A) newTeamB else newTeamA
            newServerPlayer = servingTeamState.player1
            newReceiverPlayer = receivingTeamState.player1
            newServerCourt = if ((if (newServingTeam == TeamSide.TEAM_A) newTeamAScore else newTeamBScore) % 2 == 0) CourtSide.RIGHT else CourtSide.LEFT
        } else {
            if (servingTeamScored) {
                // SCENARIO 1: Serving side won the rally
                // Same server keeps serving, serving team swaps courts (doubles)
                if (scoringTeam == TeamSide.TEAM_A) {
                    newTeamA = if (currentState.matchType == MatchType.DOUBLES) {
                        currentState.teamA.copy(score = newTeamAScore).swapPositions()
                    } else {
                        currentState.teamA.copy(score = newTeamAScore)
                    }
                    newTeamB = currentState.teamB.copy(score = newTeamBScore)
                } else {
                    newTeamA = currentState.teamA.copy(score = newTeamAScore)
                    newTeamB = if (currentState.matchType == MatchType.DOUBLES) {
                        currentState.teamB.copy(score = newTeamBScore).swapPositions()
                    } else {
                        currentState.teamB.copy(score = newTeamBScore)
                    }
                }
                newServingTeam = currentState.servingTeam
                newServerPlayer = currentState.serverPlayer
                
                // Server moves to the court matching their team's score parity
                val servingScore = if (scoringTeam == TeamSide.TEAM_A) newTeamAScore else newTeamBScore
                newServerCourt = if (servingScore % 2 == 0) CourtSide.RIGHT else CourtSide.LEFT
                
                // Receiver is the opponent on the same diagonal court
                val receivingTeamState = if (scoringTeam == TeamSide.TEAM_A) newTeamB else newTeamA
                newReceiverPlayer = receivingTeamState.playerAt(newServerCourt)
            } else {
                // SCENARIO 2: Receiving side won the rally (service changes)
                // Nobody swaps courts!
                newTeamA = currentState.teamA.copy(score = newTeamAScore)
                newTeamB = currentState.teamB.copy(score = newTeamBScore)
                newServingTeam = scoringTeam
                
                // The player on the scoring team in the correct parity court becomes server
                val newServingScore = if (scoringTeam == TeamSide.TEAM_A) newTeamAScore else newTeamBScore
                newServerCourt = if (newServingScore % 2 == 0) CourtSide.RIGHT else CourtSide.LEFT
                
                val newServingTeamState = if (scoringTeam == TeamSide.TEAM_A) newTeamA else newTeamB
                newServerPlayer = newServingTeamState.playerAt(newServerCourt)
                
                val newReceivingTeamState = if (scoringTeam == TeamSide.TEAM_A) newTeamB else newTeamA
                newReceiverPlayer = newReceivingTeamState.playerAt(newServerCourt)
            }
        }
        
        // Check match completion
        var newSetsWonA = currentState.setsWonA
        var newSetsWonB = currentState.setsWonB
        val newCompletedSets = currentState.completedSets.toMutableList()
        var isMatchOver = false
        var matchWinner: TeamSide? = null
        
        if (isSetWon) {
            if (setWinner == TeamSide.TEAM_A) newSetsWonA++ else newSetsWonB++
            newCompletedSets.add(SetScore(
                setNumber = currentState.currentSetNumber,
                teamAScore = newTeamAScore,
                teamBScore = newTeamBScore,
                winner = setWinner
            ))
            
            val setsToWin = (currentState.bestOfSets / 2) + 1
            if (isSkunk || newSetsWonA >= setsToWin || newSetsWonB >= setsToWin) {
                isMatchOver = true
                matchWinner = if (isSkunk) scoringTeam else (if (newSetsWonA >= setsToWin) TeamSide.TEAM_A else TeamSide.TEAM_B)
            }
        }
        
        return currentState.copy(
            teamA = newTeamA,
            teamB = newTeamB,
            servingTeam = newServingTeam,
            serverPlayer = newServerPlayer,
            receiverPlayer = newReceiverPlayer,
            serverCourt = newServerCourt,
            isSetOver = isSetWon,
            isMatchOver = isMatchOver,
            matchWinner = matchWinner,
            setsWonA = newSetsWonA,
            setsWonB = newSetsWonB,
            completedSets = newCompletedSets,
            rallyNumber = newRallyNumber
        )
    }
    
    /**
     * Starts a new set after the previous one ended.
     * Resets scores to 0, alternates first serve based on previous set winner.
     * 
     * @param currentState The current match state where a set has just ended.
     * @return The updated match state for the new set.
     */
    fun startNewSet(currentState: BadmintonLiveState): BadmintonLiveState {
        if (!currentState.isSetOver || currentState.isMatchOver) return currentState
        
        stateHistory.addLast(currentState)
        
        // In new set, the losing team of the previous set serves first
        val lastSetWinner = currentState.completedSets.lastOrNull()?.winner ?: TeamSide.TEAM_A
        val newServingTeam = if (lastSetWinner == TeamSide.TEAM_A) TeamSide.TEAM_B else TeamSide.TEAM_A
        
        // Reset player positions back to original order and reset scores.
        val resetTeamA = currentState.teamA.copy(score = 0)
        val resetTeamB = currentState.teamB.copy(score = 0)
        
        val servingTeamState = if (newServingTeam == TeamSide.TEAM_A) resetTeamA else resetTeamB
        val receivingTeamState = if (newServingTeam == TeamSide.TEAM_A) resetTeamB else resetTeamA
        
        return currentState.copy(
            currentSetNumber = currentState.currentSetNumber + 1,
            teamA = resetTeamA,
            teamB = resetTeamB,
            servingTeam = newServingTeam,
            serverPlayer = servingTeamState.player1,
            receiverPlayer = receivingTeamState.player1,
            serverCourt = CourtSide.RIGHT,
            isSetOver = false,
            rallyNumber = 0
        )
    }
    
    /**
     * Undoes the last point. Returns the previous state, or null if no history.
     */
    fun undo(currentState: BadmintonLiveState? = null): BadmintonLiveState? {
        if (stateHistory.isEmpty()) return null
        if (currentState != null) {
            redoHistory.addLast(currentState)
        }
        return stateHistory.removeLast()
    }

    /**
     * Redoes the previously undone state. Returns the state, or null if no redo history.
     */
    fun redo(currentState: BadmintonLiveState? = null): BadmintonLiveState? {
        if (redoHistory.isEmpty()) return null
        if (currentState != null) {
            stateHistory.addLast(currentState)
        }
        return redoHistory.removeLast()
    }
    
    /**
     * Clears the history stacks.
     */
    fun clearHistory() {
        stateHistory.clear()
        redoHistory.clear()
    }
    
    /**
     * Returns whether undo is available.
     */
    fun canUndo(): Boolean = stateHistory.isNotEmpty()

    /**
     * Returns whether redo is available.
     */
    fun canRedo(): Boolean = redoHistory.isNotEmpty()

    /**
     * Swaps the court positions (left <-> right) for the given team.
     * In BWF rules, if swapping the serving team, the player who lands on the required court
     * (RIGHT for even score, LEFT for odd score) becomes the active server.
     */
    fun swapTeamPositions(currentState: BadmintonLiveState, team: TeamSide): BadmintonLiveState {
        stateHistory.addLast(currentState)
        redoHistory.clear()
        val (newTeamA, newTeamB) = if (team == TeamSide.TEAM_A) {
            currentState.teamA.swapPositions() to currentState.teamB
        } else {
            currentState.teamA to currentState.teamB.swapPositions()
        }
        
        val servingScore = if (currentState.servingTeam == TeamSide.TEAM_A) newTeamA.score else newTeamB.score
        val requiredCourt = if (servingScore % 2 == 0) CourtSide.RIGHT else CourtSide.LEFT
        
        val server = if (currentState.servingTeam == TeamSide.TEAM_A) {
            newTeamA.playerAt(requiredCourt)
        } else {
            newTeamB.playerAt(requiredCourt)
        }
        val receiver = if (currentState.servingTeam == TeamSide.TEAM_A) {
            newTeamB.playerAt(requiredCourt)
        } else {
            newTeamA.playerAt(requiredCourt)
        }
        return currentState.copy(
            teamA = newTeamA,
            teamB = newTeamB,
            serverPlayer = server,
            receiverPlayer = receiver,
            serverCourt = requiredCourt
        )
    }

    /**
     * Toggles the serving team (switches who serves).
     * The server on the new serving team is determined by that team's score parity:
     * Even score -> Right court player serves, Odd score -> Left court player serves.
     */
    fun toggleServingTeam(currentState: BadmintonLiveState): BadmintonLiveState {
        stateHistory.addLast(currentState)
        redoHistory.clear()
        val nextServingTeam = if (currentState.servingTeam == TeamSide.TEAM_A) TeamSide.TEAM_B else TeamSide.TEAM_A
        val servingScore = if (nextServingTeam == TeamSide.TEAM_A) currentState.teamA.score else currentState.teamB.score
        val requiredCourt = if (servingScore % 2 == 0) CourtSide.RIGHT else CourtSide.LEFT
        val server = currentState.getTeam(nextServingTeam).playerAt(requiredCourt)
        val receiver = currentState.getTeam(currentState.servingTeam).playerAt(requiredCourt)
        return currentState.copy(
            servingTeam = nextServingTeam,
            serverPlayer = server,
            receiverPlayer = receiver,
            serverCourt = requiredCourt
        )
    }

    /**
     * Directly sets a specific player as the server.
     * In accordance with BWF odd/even rules:
     * Even score -> server MUST serve from RIGHT court.
     * Odd score -> server MUST serve from LEFT court.
     * If the selected player is not on the required court, partner positions swap so the chosen server
     * is placed on the required court.
     */
    fun setServer(currentState: BadmintonLiveState, player: PlayerInfo): BadmintonLiveState {
        stateHistory.addLast(currentState)
        redoHistory.clear()
        val isTeamA = currentState.teamA.player1.id == player.id || currentState.teamA.player2?.id == player.id
        val team = if (isTeamA) TeamSide.TEAM_A else TeamSide.TEAM_B
        val teamState = currentState.getTeam(team)
        
        val requiredCourt = if (teamState.score % 2 == 0) CourtSide.RIGHT else CourtSide.LEFT
        
        val newTeamState = if (teamState.playerAt(requiredCourt).id != player.id) {
            teamState.swapPositions()
        } else {
            teamState
        }
        
        val (newTeamA, newTeamB) = if (team == TeamSide.TEAM_A) {
            newTeamState to currentState.teamB
        } else {
            currentState.teamA to newTeamState
        }
        val oppTeamState = if (team == TeamSide.TEAM_A) newTeamB else newTeamA
        val receiver = oppTeamState.playerAt(requiredCourt)
        
        return currentState.copy(
            teamA = newTeamA,
            teamB = newTeamB,
            servingTeam = team,
            serverPlayer = player,
            receiverPlayer = receiver,
            serverCourt = requiredCourt
        )
    }

    /**
     * Swaps the ends of the court (flips visual top/bottom sides).
     */
    fun toggleCourtSides(currentState: BadmintonLiveState): BadmintonLiveState {
        return currentState.copy(isSidesSwapped = !currentState.isSidesSwapped)
    }
}
