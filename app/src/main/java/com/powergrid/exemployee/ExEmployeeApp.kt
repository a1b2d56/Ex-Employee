package com.powergrid.exemployee

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
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

        // Dynamic Colors (Material You) only for SYSTEM/LIGHT/DARK/MIDNIGHT on A12+.
        // Custom themes (Phantom, Obsidian, etc.) use hardcoded overlays instead.
        if (ThemePrefs.shouldApplyDynamicColors(theme)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}
