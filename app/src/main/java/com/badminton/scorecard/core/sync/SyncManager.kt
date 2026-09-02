package com.badminton.scorecard.core.sync

import android.content.Context
import com.badminton.scorecard.core.database.dao.MatchDao
import com.badminton.scorecard.core.database.dao.PlayerDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao,
    @ApplicationContext private val context: Context
) {
    private val firestore: FirebaseFirestore? by lazy { 
        try { Firebase.firestore } catch (e: Exception) { null } 
    }
    private val auth: FirebaseAuth? by lazy { 
        try { Firebase.auth } catch (e: Exception) { null } 
    }
    
    /**
     * Syncs a completed match to Firestore.
     * Called after match completion.
     */
    suspend fun syncMatch(matchId: Long) {
        try {
            val user = auth?.currentUser ?: return
            val db = firestore ?: return
            val match = matchDao.getMatchById(matchId) ?: return
            // Upload to users/{uid}/matches/{matchId}
            db.collection("users")
                .document(user.uid)
                .collection("matches")
                .document(matchId.toString())
                .set(match) // Assuming MatchEntity is serializable to map, or convert to map
                .await()
        } catch (e: Exception) {
            // Handle failure gracefully
            e.printStackTrace()
        }
    }
    
    /**
     * Syncs a player to Firestore.
     */
    suspend fun syncPlayer(playerId: Long) {
        try {
            val user = auth?.currentUser ?: return
            val db = firestore ?: return
            val player = playerDao.getPlayerById(playerId) ?: return
            
            db.collection("users")
                .document(user.uid)
                .collection("players")
                .document(playerId.toString())
                .set(player)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Pulls all data from Firestore for the current user.
     * Used for initial sync on new device.
     */
    suspend fun pullFromCloud() {
        try {
            val user = auth?.currentUser ?: return
            val db = firestore ?: return
            // Fetch all matches, players from Firestore
            // Merge into local Room database
            // Note: Add proper synchronization logic mapping documents back to Room entities.
        } catch (e: Exception) {
            e.printStackTrace()
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
    
    /**
     * Signs in anonymously for zero-friction start.
     */
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val firebaseAuth = auth ?: throw IllegalStateException("Firebase Auth not initialized")
            val result = firebaseAuth.signInAnonymously().await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
