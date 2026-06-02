package com.powergrid.exemployee.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.powergrid.exemployee.common.AuthPrefs
import com.powergrid.exemployee.common.FontPrefs
import com.powergrid.exemployee.common.ThemePrefs
import com.powergrid.exemployee.ui.auth.LoginActivity
import com.powergrid.exemployee.ui.theme.ExEmployeeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TOKEN = "auth_token"
    }

    private var themeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null
    private var fontListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val authToken = intent.getStringExtra(EXTRA_TOKEN) ?: ""

        setContent {
            val context = LocalContext.current
            // Observe theme/font prefs reactively with SharedPreferences listeners
            var appTheme by remember { mutableStateOf(ThemePrefs.getTheme(context)) }
            var fontScale by remember { mutableFloatStateOf(FontPrefs.getScale(context)) }
            var isBold by remember { mutableStateOf(FontPrefs.isBold(context)) }
            var fontFamilyKey by remember { mutableStateOf(FontPrefs.getFontFamily(context)) }

            DisposableEffect(context) {
                val themePrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                val fontPrefs = context.getSharedPreferences("font_prefs", Context.MODE_PRIVATE)

                themeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "app_theme") {
                        appTheme = ThemePrefs.getTheme(context)
                    }
                }
                fontListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "font_scale" || key == "font_bold" || key == "font_family") {
                        fontScale = FontPrefs.getScale(context)
                        isBold = FontPrefs.isBold(context)
                        fontFamilyKey = FontPrefs.getFontFamily(context)
                    }
                }

                themePrefs.registerOnSharedPreferenceChangeListener(themeListener!!)
                fontPrefs.registerOnSharedPreferenceChangeListener(fontListener!!)

                onDispose {
                    themePrefs.unregisterOnSharedPreferenceChangeListener(themeListener)
                    fontPrefs.unregisterOnSharedPreferenceChangeListener(fontListener)
                }
            }

            ExEmployeeTheme(appTheme = appTheme, fontScale = fontScale, isBold = isBold, fontFamilyKey = fontFamilyKey) {
                MainScreen(
                    authToken = authToken,
                    onSignOut = {
                        AuthPrefs.setToken(context, null)
                        startActivity(Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                )
            }
        }
    }
}
