package com.powergrid.exemployee.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.powergrid.exemployee.common.AuthPrefs
import com.powergrid.exemployee.common.FontPrefs
import com.powergrid.exemployee.common.ThemePrefs
import com.powergrid.exemployee.security.BiometricHelper
import com.powergrid.exemployee.security.BiometricResult
import com.powergrid.exemployee.ui.main.MainActivity
import com.powergrid.exemployee.ui.theme.ExEmployeeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    @Inject lateinit var biometric: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val appTheme = ThemePrefs.getTheme(context)
            val fontScale = FontPrefs.getScale(context)
            val isBold = FontPrefs.isBold(context)

            ExEmployeeTheme(appTheme = appTheme, fontScale = fontScale, isBold = isBold) {
                LoginScreen(onLoginSuccess = { token -> navigateToMain(token) })
            }
        }

        tryBiometricAutoLogin()
    }

    private fun tryBiometricAutoLogin() {
        if (biometric.isAvailable() && biometric.hasStoredSecret()) {
            biometric.promptToDecrypt(this) { result ->
                when (result) {
                    is BiometricResult.Success -> navigateToMain(String(result.data, Charsets.UTF_8))
                    else -> Unit
                }
            }
        } else {
            val savedToken = AuthPrefs.getToken(this)
            if (!savedToken.isNullOrEmpty()) navigateToMain(savedToken)
        }
    }

    private fun navigateToMain(token: String) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_TOKEN, token)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
