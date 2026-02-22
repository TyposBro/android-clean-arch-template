package com.example.app.core.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

/**
 * TokenManager with robust handling for EncryptedSharedPreferences failures.
 *
 * The AEADBadTagException typically occurs when:
 * 1. The app was restored from backup but the encryption key wasn't
 * 2. The device's KeyStore was corrupted
 * 3. The encrypted file was corrupted
 *
 * Our strategy: If encryption fails, wipe and recreate. If that fails,
 * fall back to in-memory storage for this session only (user will need to re-login).
 */
@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val TAG = "TokenManager"
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val EXPIRES_AT_KEY = "expires_at"
        private const val PREFS_FILENAME = "auth_prefs"
    }

    // In-memory fallback if encryption is completely broken
    private var inMemoryToken: String? = null
    private var inMemoryRefreshToken: String? = null
    private var inMemoryExpiresAt: Long = 0L
    private var usingFallback = false

    // Lazy initialization with robust error handling
    private val sharedPreferences: SharedPreferences? by lazy {
        getEncryptedSharedPreferences()
    }

    // Eagerly initialize on a background thread to prevent ANRs on first access
    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Just accessing the property triggers the lazy block
                val prefs = sharedPreferences
                Log.d(TAG, "TokenManager initialized successfully on background thread")
            } catch (e: Exception) {
                Log.e(TAG, "TokenManager background init failed", e)
            }
        }
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences? {
        return try {
            createSharedPreferences()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating EncryptedSharedPreferences: ${e.javaClass.simpleName}", e)

            // If it's an AEAD error, wipe and retry
            if (e is AEADBadTagException ||
                e is GeneralSecurityException ||
                e.cause is AEADBadTagException ||
                e.cause is GeneralSecurityException) {

                Log.w(TAG, "Detected crypto corruption. Wiping encrypted storage and retrying...")
                deleteSharedPreferences()

                try {
                    return createSharedPreferences()
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to recreate EncryptedSharedPreferences after wipe", e2)
                }
            }

            // Fall back to in-memory mode - user will need to re-login but app won't crash
            Log.w(TAG, "Falling back to in-memory token storage. User will need to re-login.")
            usingFallback = true
            null
        }
    }

    private fun createSharedPreferences(): SharedPreferences {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
             throw GeneralSecurityException("Failed to create MasterKey or SharedPreferences", e)
        }
    }

    private fun deleteSharedPreferences() {
        try {
            // 1. Delete the SharedPreferences file cleanly (API 24+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val deleted = context.deleteSharedPreferences(PREFS_FILENAME)
                 Log.d(TAG, "Deleted prefs via context.deleteSharedPreferences: $deleted")
            } else {
                 val prefsFile = File(context.filesDir.parent, "shared_prefs/$PREFS_FILENAME.xml")
                if (prefsFile.exists()) {
                    val deleted = prefsFile.delete()
                    Log.d(TAG, "Deleted prefs file manually: $deleted")
                }
            }

            // 2. Delete the Master Key from Android KeyStore
            // This is critical for AEADBadTagException. If we don't delete the alias,
            // the new file will try to use the old (bad) key.
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (keyStore.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                Log.d(TAG, "Deleted master key from KeyStore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing corrupted preferences", e)
        }
    }

    // --- Access Token ---
    fun saveToken(token: String) {
        if (usingFallback) {
            inMemoryToken = token
            return
        }
        try {
            sharedPreferences?.edit(commit = true) { putString(AUTH_TOKEN_KEY, token) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save token, using in-memory fallback", e)
            inMemoryToken = token
        }
    }

    fun getToken(): String? {
        if (usingFallback) return inMemoryToken
        return try {
            sharedPreferences?.getString(AUTH_TOKEN_KEY, null) ?: inMemoryToken
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read token, returning in-memory value", e)
            inMemoryToken
        }
    }

    fun clearToken() {
        inMemoryToken = null
        try {
            sharedPreferences?.edit(commit = true) { remove(AUTH_TOKEN_KEY) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear token", e)
        }
    }

    fun hasToken(): Boolean {
        return getToken() != null
    }

    // --- Refresh Token ---
    fun saveRefreshToken(token: String) {
        if (usingFallback) {
            inMemoryRefreshToken = token
            return
        }
        try {
            sharedPreferences?.edit(commit = true) { putString(REFRESH_TOKEN_KEY, token) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save refresh token, using in-memory fallback", e)
            inMemoryRefreshToken = token
        }
    }

    fun getRefreshToken(): String? {
        if (usingFallback) return inMemoryRefreshToken
        return try {
            sharedPreferences?.getString(REFRESH_TOKEN_KEY, null) ?: inMemoryRefreshToken
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read refresh token, returning in-memory value", e)
            inMemoryRefreshToken
        }
    }

    fun clearRefreshToken() {
        inMemoryRefreshToken = null
        try {
            sharedPreferences?.edit(commit = true) { remove(REFRESH_TOKEN_KEY) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear refresh token", e)
        }
    }

    // --- Expiration Timestamp ---
    fun saveExpiresAt(timestamp: Long) {
        if (usingFallback) {
            inMemoryExpiresAt = timestamp
            return
        }
        try {
            sharedPreferences?.edit(commit = true) { putLong(EXPIRES_AT_KEY, timestamp) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save expiresAt, using in-memory fallback", e)
            inMemoryExpiresAt = timestamp
        }
    }

    fun getExpiresAt(): Long {
        if (usingFallback) return inMemoryExpiresAt
        return try {
            sharedPreferences?.getLong(EXPIRES_AT_KEY, 0L) ?: inMemoryExpiresAt
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read expiresAt, returning in-memory value", e)
            inMemoryExpiresAt
        }
    }

    fun clearExpiresAt() {
        inMemoryExpiresAt = 0L
        try {
            sharedPreferences?.edit(commit = true) { remove(EXPIRES_AT_KEY) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear expiresAt", e)
        }
    }

    // Helper to clear everything on logout
    fun clearAll() {
        inMemoryToken = null
        inMemoryRefreshToken = null
        inMemoryExpiresAt = 0L
        try {
            sharedPreferences?.edit(commit = true) {
                remove(AUTH_TOKEN_KEY)
                remove(REFRESH_TOKEN_KEY)
                remove(EXPIRES_AT_KEY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all tokens", e)
        }
    }
}
