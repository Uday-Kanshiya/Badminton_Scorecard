package com.badminton.scorecard.core.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth?
) {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    private val _isSignedIn = MutableStateFlow(auth?.currentUser != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()
    
    init {
        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            _isSignedIn.value = firebaseAuth.currentUser != null
        }
    }
    
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            if (auth == null) throw IllegalStateException("Firebase Auth not initialized")
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw IllegalStateException("User is null")
            _currentUser.value = user
            _isSignedIn.value = true
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        // Implement Google Sign-In logic here using idToken if necessary
        return Result.failure(NotImplementedError("Google Sign In not implemented yet"))
    }
    
    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
