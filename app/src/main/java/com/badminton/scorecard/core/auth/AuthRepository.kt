package com.badminton.scorecard.core.auth

import android.content.Context
import android.content.Intent
import com.badminton.scorecard.core.preferences.ThemePreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleAccountProfile(
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val id: String
)

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth?,
    private val themePreferences: ThemePreferences
) {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _googleProfile = MutableStateFlow<GoogleAccountProfile?>(null)
    val googleProfile: StateFlow<GoogleAccountProfile?> = _googleProfile.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val savedEmail = themePreferences.googleUserEmail.firstOrNull()
            if (!savedEmail.isNullOrBlank()) {
                val savedName = themePreferences.googleUserName.firstOrNull()
                val savedPhoto = themePreferences.googleUserPhoto.firstOrNull()
                val profile = GoogleAccountProfile(
                    email = savedEmail,
                    displayName = savedName,
                    photoUrl = savedPhoto,
                    id = savedEmail
                )
                _googleProfile.value = profile
                _isSignedIn.value = true
            } else if (auth?.currentUser != null) {
                _isSignedIn.value = true
            }
        }

        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            if (firebaseAuth.currentUser != null) {
                _isSignedIn.value = true
            }
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun handleSignInResult(data: Intent?): Result<GoogleAccountProfile> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.await()
            val email = account.email ?: throw IllegalStateException("Google account has no email")
            val profile = GoogleAccountProfile(
                email = email,
                displayName = account.displayName,
                photoUrl = account.photoUrl?.toString(),
                id = account.id ?: email
            )

            // Try Firebase credential link if idToken is available, or sign in anonymously as fallback
            try {
                if (account.idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth?.signInWithCredential(credential)?.await()
                } else if (auth?.currentUser == null) {
                    auth?.signInAnonymously()?.await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            themePreferences.saveGoogleUser(profile.email, profile.displayName, profile.photoUrl)
            _googleProfile.value = profile
            _isSignedIn.value = true
            Result.success(profile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun signInWithManualEmail(email: String, displayName: String?): Result<GoogleAccountProfile> {
        return try {
            val profile = GoogleAccountProfile(
                email = email.trim(),
                displayName = displayName?.trim(),
                photoUrl = null,
                id = email.trim()
            )
            themePreferences.saveGoogleUser(profile.email, profile.displayName, null)
            _googleProfile.value = profile
            _isSignedIn.value = true
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(context, gso).signOut().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            auth?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        themePreferences.clearGoogleUser()
        _googleProfile.value = null
        _isSignedIn.value = false
    }
}
