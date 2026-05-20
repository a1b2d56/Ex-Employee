package com.powergrid.exemployee.common

import android.content.Context
import androidx.core.content.edit

object AuthPrefs {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_TOKEN, token)
        }
    }

    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_TOKEN)
        }
    }
}
