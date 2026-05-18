package com.powergrid.exemployee.common

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

/**
 * Persists the user's chosen theme.
 *
 * Themes that follow Android Dynamic Colors (Material You on A12+, sky-blue fallback on A11-):
 *   LIGHT, DARK, MIDNIGHT
 *
 * Custom themes always use their own hardcoded palette regardless of API level:
 *   PHANTOM, OBSIDIAN, ESPRESSO, MATCHA, NORD, ROSE
 */
object ThemePrefs {
    private const val PREFS = "theme_prefs"
    private const val KEY_THEME = "app_theme"

    enum class AppTheme(val label: String) {
        LIGHT("Light"),
        DARK("Dark"),
        MIDNIGHT("Midnight"),
        PHANTOM("Phantom"),
        OBSIDIAN("Obsidian"),
        ESPRESSO("Espresso"),
        MATCHA("Matcha"),
        NORD("Nord"),
        ROSE("Rosé");

        /** True when the theme tracks the system and uses Dynamic Colors on A12+ */
        val usesDynamicColors: Boolean
            get() = this == LIGHT || this == DARK || this == MIDNIGHT
    }

    fun setTheme(ctx: Context, theme: AppTheme) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY_THEME, theme.name) }
    }

    fun getTheme(ctx: Context): AppTheme {
        val name = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME, null)
        return name?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.LIGHT
    }

    /** Resolves the NightMode flag for AppCompatDelegate based on the current theme choice. */
    fun resolveNightMode(theme: AppTheme): Int = when (theme) {
        AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        AppTheme.DARK, AppTheme.MIDNIGHT,
        AppTheme.PHANTOM, AppTheme.OBSIDIAN, AppTheme.ESPRESSO,
        AppTheme.MATCHA, AppTheme.NORD, AppTheme.ROSE -> AppCompatDelegate.MODE_NIGHT_YES
    }

    /** Whether Dynamic Colors should be applied (only for LIGHT/DARK/MIDNIGHT on A12+). */
    fun shouldApplyDynamicColors(theme: AppTheme): Boolean =
        theme.usesDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}
