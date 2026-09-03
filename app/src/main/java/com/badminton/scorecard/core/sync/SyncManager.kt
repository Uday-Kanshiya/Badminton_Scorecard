package com.badminton.scorecard.core.sync

import android.content.Context
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.badminton.scorecard.core.database.entity.MatchEntity
import com.badminton.scorecard.core.database.entity.MatchEventEntity
import com.badminton.scorecard.core.database.entity.MatchPlayerCrossRef
import com.badminton.scorecard.core.database.entity.PlayerEntity
import com.badminton.scorecard.core.database.entity.PlayerStatsCacheEntity
import com.badminton.scorecard.core.database.entity.SetEntity
import com.badminton.scorecard.core.preferences.ThemePreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class SyncSummary(
    val success: Boolean,
    val playersCount: Int = 0,
    val matchesCount: Int = 0,
    val message: String = ""
)

@Singleton
class SyncManager @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao,
    private val themePreferences: ThemePreferences,
    @ApplicationContext private val context: Context
) {
    private val firestore: FirebaseFirestore? by lazy { 
        try { Firebase.firestore } catch (e: Exception) { null } 
    }
    private val auth: FirebaseAuth? by lazy { 
        try { Firebase.auth } catch (e: Exception) { null } 
    }

    private suspend fun getUserAccountKey(): String? {
        val savedEmail = themePreferences.googleUserEmail.firstOrNull()
        if (!savedEmail.isNullOrBlank()) return savedEmail
        val fbEmail = auth?.currentUser?.email
        if (!fbEmail.isNullOrBlank()) return fbEmail
        return auth?.currentUser?.uid
    }
    
    /**
     * Syncs a completed match to Firestore under the current user's Google account.
     */
    suspend fun syncMatch(matchId: Long) {
        try {
            val accountKey = getUserAccountKey() ?: return
            val db = firestore ?: return
            val match = matchDao.getMatchById(matchId).firstOrNull() ?: return
            val matchPlayers = matchDao.getMatchPlayersForMatch(matchId).firstOrNull() ?: emptyList()
            val sets = matchDao.getSetsForMatch(matchId).firstOrNull() ?: emptyList()
            val events = matchDao.getEventsForMatch(matchId).firstOrNull() ?: emptyList()

            val matchDoc = hashMapOf<String, Any?>(
                "id" to match.id,
                "matchType" to match.matchType,
                "targetPoints" to match.targetPoints,
                "bestOfSets" to match.bestOfSets,
                "skunkRuleEnabled" to match.skunkRuleEnabled,
                "status" to match.status,
                "winnerTeam" to match.winnerTeam,
                "startedAt" to match.startedAt,
                "endedAt" to match.endedAt,
                "serviceRotationEnabled" to match.serviceRotationEnabled,
                "playerPointAttribution" to match.playerPointAttribution,
                "players" to matchPlayers.map {
                    mapOf(
                        "matchId" to it.matchId,
                        "playerId" to it.playerId,
                        "team" to it.team,
                        "playerOrder" to it.playerOrder
                    )
                },
                "sets" to sets.map {
                    mapOf(
                        "id" to it.id,
                        "matchId" to it.matchId,
                        "setNumber" to it.setNumber,
                        "teamAScore" to it.teamAScore,
                        "teamBScore" to it.teamBScore,
                        "winnerTeam" to it.winnerTeam,
                        "initialServerPlayerId" to it.initialServerPlayerId,
                        "startedAt" to it.startedAt,
                        "endedAt" to it.endedAt
                    )
                },
                "events" to events.map {
                    mapOf(
                        "id" to it.id,
                        "setId" to it.setId,
                        "rallyNumber" to it.rallyNumber,
                        "scoringTeam" to it.scoringTeam,
                        "servingPlayerId" to it.servingPlayerId,
                        "serverCourt" to it.serverCourt,
                        "teamAScoreAfter" to it.teamAScoreAfter,
                        "teamBScoreAfter" to it.teamBScoreAfter,
                        "scoringPlayerId" to it.scoringPlayerId,
                        "timestamp" to it.timestamp
                    )
                }
            )

            db.collection("users")
                .document(accountKey)
                .collection("matches")
                .document(matchId.toString())
                .set(matchDoc)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Syncs a player to Firestore under the current user's Google account.
     */
    suspend fun syncPlayer(playerId: Long) {
        try {
            val accountKey = getUserAccountKey() ?: return
            val db = firestore ?: return
            val player = playerDao.getPlayerById(playerId).firstOrNull() ?: return
            
            val playerDoc = hashMapOf<String, Any?>(
                "id" to player.id,
                "name" to player.name,
                "nickname" to player.nickname,
                "createdAt" to player.createdAt
            )

            db.collection("users")
                .document(accountKey)
                .collection("players")
                .document(playerId.toString())
                .set(playerDoc)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Uploads all local records to the user's Google cloud store.
     */
    suspend fun backupAllToCloud(): SyncSummary {
        return try {
            val accountKey = getUserAccountKey() ?: return SyncSummary(false, message = "Not signed in to Google Cloud")
            val db = firestore ?: return SyncSummary(false, message = "Cloud storage service unavailable")

            val players = playerDao.getAllPlayers().firstOrNull() ?: emptyList()
            val matches = matchDao.getAllMatches().firstOrNull() ?: emptyList()

            for (player in players) {
                syncPlayer(player.id)
            }
            for (match in matches) {
                syncMatch(match.id)
            }

            val stats = playerDao.getAllPlayerStats().firstOrNull() ?: emptyList()
            for (stat in stats) {
                val statDoc = hashMapOf<String, Any?>(
                    "playerId" to stat.playerId,
                    "totalMatchesPlayed" to stat.totalMatchesPlayed,
                    "totalWins" to stat.totalWins,
                    "totalLosses" to stat.totalLosses,
                    "singlesPlayed" to stat.singlesPlayed,
                    "singlesWon" to stat.singlesWon,
                    "doublesPlayed" to stat.doublesPlayed,
                    "doublesWon" to stat.doublesWon,
                    "totalPointsScored" to stat.totalPointsScored,
                    "totalPointsOnServe" to stat.totalPointsOnServe,
                    "totalServeRallies" to stat.totalServeRallies,
                    "totalPointsOnReturn" to stat.totalPointsOnReturn,
                    "totalReturnRallies" to stat.totalReturnRallies,
                    "individualPointsScored" to stat.individualPointsScored,
                    "lastUpdated" to stat.lastUpdated
                )
                db.collection("users").document(accountKey).collection("stats").document(stat.playerId.toString()).set(statDoc).await()
            }

            val nowStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
            themePreferences.setLastSyncTime(nowStr)
            SyncSummary(true, players.size, matches.size, "Backed up ${matches.size} matches and ${players.size} players to Google Cloud.")
        } catch (e: Exception) {
            e.printStackTrace()
            SyncSummary(false, message = "Cloud backup failed: ${e.message}")
        }
    }
    
    /**
     * Pulls and caches all previously saved players, matches, sets, events, and stats from the user's Google Cloud account into local Room database.
     */
    suspend fun pullFromCloud(): SyncSummary {
        return try {
            val accountKey = getUserAccountKey() ?: return SyncSummary(false, message = "Not signed in to Google Cloud")
            val db = firestore ?: return SyncSummary(false, message = "Cloud storage service unavailable")

            // 1. Pull and cache Players
            val playersSnap = db.collection("users").document(accountKey).collection("players").get().await()
            var restoredPlayers = 0
            for (doc in playersSnap.documents) {
                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                val name = doc.getString("name") ?: continue
                val nickname = doc.getString("nickname")
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                playerDao.insertPlayer(PlayerEntity(id = id, name = name, nickname = nickname, createdAt = createdAt))
                restoredPlayers++
            }

            // 2. Pull and cache Matches, Sets, Events, Players
            val matchesSnap = db.collection("users").document(accountKey).collection("matches").get().await()
            var restoredMatches = 0
            for (doc in matchesSnap.documents) {
                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                val matchType = doc.getString("matchType") ?: "SINGLES"
                val targetPoints = doc.getLong("targetPoints")?.toInt() ?: 21
                val bestOfSets = doc.getLong("bestOfSets")?.toInt() ?: 1
                val skunkRuleEnabled = doc.getBoolean("skunkRuleEnabled") ?: false
                val status = doc.getString("status") ?: "COMPLETED"
                val winnerTeam = doc.getString("winnerTeam")
                val startedAt = doc.getLong("startedAt") ?: System.currentTimeMillis()
                val endedAt = doc.getLong("endedAt")
                val serviceRotationEnabled = doc.getBoolean("serviceRotationEnabled") ?: true
                val playerPointAttribution = doc.getBoolean("playerPointAttribution") ?: false

                val matchEntity = MatchEntity(
                    id = id,
                    matchType = matchType,
                    targetPoints = targetPoints,
                    bestOfSets = bestOfSets,
                    skunkRuleEnabled = skunkRuleEnabled,
                    status = status,
                    winnerTeam = winnerTeam,
                    startedAt = startedAt,
                    endedAt = endedAt,
                    serviceRotationEnabled = serviceRotationEnabled,
                    playerPointAttribution = playerPointAttribution
                )
                matchDao.insertMatch(matchEntity)

                // Match Players
                @Suppress("UNCHECKED_CAST")
                val playersList = doc.get("players") as? List<Map<String, Any?>>
                if (playersList != null) {
                    for (p in playersList) {
                        val pMatchId = (p["matchId"] as? Number)?.toLong() ?: id
                        val pPlayerId = (p["playerId"] as? Number)?.toLong() ?: continue
                        val pTeam = p["team"] as? String ?: "TEAM_A"
                        val pOrder = (p["playerOrder"] as? Number)?.toInt() ?: 1
                        matchDao.insertMatchPlayers(MatchPlayerCrossRef(pMatchId, pPlayerId, pTeam, pOrder))
                    }
                }

                // Sets
                @Suppress("UNCHECKED_CAST")
                val setsList = doc.get("sets") as? List<Map<String, Any?>>
                if (setsList != null) {
                    for (s in setsList) {
                        val sId = (s["id"] as? Number)?.toLong() ?: 0L
                        val sMatchId = (s["matchId"] as? Number)?.toLong() ?: id
                        val sNumber = (s["setNumber"] as? Number)?.toInt() ?: 1
                        val sScoreA = (s["teamAScore"] as? Number)?.toInt() ?: 0
                        val sScoreB = (s["teamBScore"] as? Number)?.toInt() ?: 0
                        val sWinner = s["winnerTeam"] as? String
                        val sServer = (s["initialServerPlayerId"] as? Number)?.toLong() ?: 0L
                        val sStart = (s["startedAt"] as? Number)?.toLong() ?: startedAt
                        val sEnd = (s["endedAt"] as? Number)?.toLong()
                        matchDao.insertSet(SetEntity(sId, sMatchId, sNumber, sScoreA, sScoreB, sWinner, sServer, sStart, sEnd))
                    }
                }

                // Events
                @Suppress("UNCHECKED_CAST")
                val eventsList = doc.get("events") as? List<Map<String, Any?>>
                if (eventsList != null) {
                    for (e in eventsList) {
                        val eId = (e["id"] as? Number)?.toLong() ?: 0L
                        val eSetId = (e["setId"] as? Number)?.toLong() ?: continue
                        val eRally = (e["rallyNumber"] as? Number)?.toInt() ?: 1
                        val eTeam = e["scoringTeam"] as? String ?: "TEAM_A"
                        val eServer = (e["servingPlayerId"] as? Number)?.toLong() ?: 0L
                        val eCourt = e["serverCourt"] as? String ?: "RIGHT"
                        val eScoreA = (e["teamAScoreAfter"] as? Number)?.toInt() ?: 0
                        val eScoreB = (e["teamBScoreAfter"] as? Number)?.toInt() ?: 0
                        val eScoringPlayer = (e["scoringPlayerId"] as? Number)?.toLong()
                        val eTimestamp = (e["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        matchDao.insertEvent(MatchEventEntity(eId, eSetId, eRally, eTeam, eServer, eCourt, eScoreA, eScoreB, eScoringPlayer, eTimestamp))
                    }
                }
                restoredMatches++
            }

            // 3. Pull Stats Cache
            val statsSnap = db.collection("users").document(accountKey).collection("stats").get().await()
            for (doc in statsSnap.documents) {
                val pId = doc.getLong("playerId") ?: doc.id.toLongOrNull() ?: continue
                val played = doc.getLong("totalMatchesPlayed")?.toInt() ?: 0
                val won = doc.getLong("totalWins")?.toInt() ?: 0
                val lost = doc.getLong("totalLosses")?.toInt() ?: 0
                val sPlayed = doc.getLong("singlesPlayed")?.toInt() ?: 0
                val sWon = doc.getLong("singlesWon")?.toInt() ?: 0
                val dPlayed = doc.getLong("doublesPlayed")?.toInt() ?: 0
                val dWon = doc.getLong("doublesWon")?.toInt() ?: 0
                val ptsScored = doc.getLong("totalPointsScored")?.toInt() ?: 0
                val ptsOnServe = doc.getLong("totalPointsOnServe")?.toInt() ?: 0
                val serveRallies = doc.getLong("totalServeRallies")?.toInt() ?: 0
                val ptsOnReturn = doc.getLong("totalPointsOnReturn")?.toInt() ?: 0
                val returnRallies = doc.getLong("totalReturnRallies")?.toInt() ?: 0
                val indPts = doc.getLong("individualPointsScored")?.toInt() ?: 0
                val updated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()
                playerDao.insertOrUpdateStats(
                    PlayerStatsCacheEntity(
                        playerId = pId,
                        totalMatchesPlayed = played,
                        totalWins = won,
                        totalLosses = lost,
                        singlesPlayed = sPlayed,
                        singlesWon = sWon,
                        doublesPlayed = dPlayed,
                        doublesWon = dWon,
                        totalPointsScored = ptsScored,
                        totalPointsOnServe = ptsOnServe,
                        totalServeRallies = serveRallies,
                        totalPointsOnReturn = ptsOnReturn,
                        totalReturnRallies = returnRallies,
                        individualPointsScored = indPts,
                        lastUpdated = updated
                    )
                )
            }

            val nowStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
            themePreferences.setLastSyncTime(nowStr)
            SyncSummary(true, restoredPlayers, restoredMatches, "Successfully cached $restoredMatches matches & $restoredPlayers players from Google Cloud!")
        } catch (e: Exception) {
            e.printStackTrace()
            SyncSummary(false, message = "Cloud restoration failed: ${e.message}")
        }
    }
    
    /**
     * Enables offline persistence for Firestore.
     */
    fun enableOfflineMode() {
        try {
            val settings = firestoreSettings {
                isPersistenceEnabled = true
            }
            firestore?.firestoreSettings = settings
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
