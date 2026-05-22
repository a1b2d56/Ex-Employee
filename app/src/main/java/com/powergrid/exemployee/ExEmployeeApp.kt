package com.powergrid.exemployee

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

import com.powergrid.exemployee.common.ThemePrefs
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExEmployeeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applyTheme()
    }

    fun applyTheme() {
        val theme = ThemePrefs.getTheme(this)

        // Set night mode based on chosen theme
        AppCompatDelegate.setDefaultNightMode(ThemePrefs.resolveNightMode(theme))

        // Compose handles dynamic colors now via ExEmployeeTheme
    }
}
