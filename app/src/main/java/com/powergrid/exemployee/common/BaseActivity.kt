package com.powergrid.exemployee.common

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.powergrid.exemployee.R

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeOverlay()
        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        val scale  = FontPrefs.getScale(newBase)
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = scale
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    /**
     * Applies the correct theme overlay based on the user's theme preference.
     *
     * Custom themes always get their hardcoded overlay.
     * Midnight: on A12+ gets the Midnight overlay (pure black surfaces, dynamic accent);
     *           on A11- gets MidnightFallback (pure black + sky-blue accent).
     * Light/Dark: on A12+ DynamicColors handles everything — no overlay.
     *             on A11- gets the sky-blue fallback overlay.
     */
    private fun applyThemeOverlay() {
        val theme = ThemePrefs.getTheme(this)
        val isPreS = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

        val overlayRes = when (theme) {
            ThemePrefs.AppTheme.MIDNIGHT  -> if (isPreS) R.style.ThemeOverlay_ExEmployee_MidnightFallback
                                            else R.style.ThemeOverlay_ExEmployee_Midnight
            ThemePrefs.AppTheme.PHANTOM   -> R.style.ThemeOverlay_ExEmployee_Phantom
            ThemePrefs.AppTheme.OBSIDIAN  -> R.style.ThemeOverlay_ExEmployee_Obsidian
            ThemePrefs.AppTheme.ESPRESSO  -> R.style.ThemeOverlay_ExEmployee_Espresso
            ThemePrefs.AppTheme.MATCHA    -> R.style.ThemeOverlay_ExEmployee_Matcha
            ThemePrefs.AppTheme.NORD      -> R.style.ThemeOverlay_ExEmployee_Nord
            ThemePrefs.AppTheme.ROSE      -> R.style.ThemeOverlay_ExEmployee_Rose
            else -> {
                // Light/Dark: on pre-A12 apply full fallback (accents + surfaces).
                // On A12+ apply surface-only overlay (DynamicColors handles accents).
                val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                if (isPreS) {
                    if (isDark) R.style.ThemeOverlay_ExEmployee_FallbackDark
                    else R.style.ThemeOverlay_ExEmployee_FallbackLight
                } else {
                    if (isDark) R.style.ThemeOverlay_ExEmployee_SurfaceDark
                    else R.style.ThemeOverlay_ExEmployee_SurfaceLight
                }
            }
        }
        overlayRes?.let { getTheme().applyStyle(it, true) }
    }
}
