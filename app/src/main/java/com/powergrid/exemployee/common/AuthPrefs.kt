package com.powergrid.exemployee.common

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object AuthPrefs {
    private const val PREFS_NAME = "auth_prefs_secure"
    private const val KEY_TOKEN = "auth_token"

    @Volatile
    private var sharedPrefs: SharedPreferences? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return sharedPrefs ?: synchronized(this) {
            sharedPrefs ?: run {
                try {
                    val appContext = context.applicationContext
                    val masterKey = MasterKey.Builder(appContext)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    EncryptedSharedPreferences.create(
                        appContext,
                        PREFS_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    ).also { sharedPrefs = it }
                } catch (e: Exception) {
                    // Fallback to standard SharedPreferences if Keystore fails (e.g. on emulators or custom ROMs)
                    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                }
            }
        }
    }

    fun saveToken(context: Context, token: String) {
        getPrefs(context).edit {
            putString(KEY_TOKEN, token)
        }
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        getPrefs(context).edit {
            remove(KEY_TOKEN)
        }
    }

    fun setToken(context: Context, token: String?) {
        if (token == null) clearToken(context) else saveToken(context, token)
    }
}
