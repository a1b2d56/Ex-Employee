package com.powergrid.exemployee.presentation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.powergrid.exemployee.R
import com.powergrid.exemployee.common.BaseActivity
import com.powergrid.exemployee.common.FontPrefs
import com.powergrid.exemployee.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    companion object {
        const val EXTRA_TOKEN = "auth_token"
        val FONT_SCALES = floatArrayOf(0.85f, 1.0f, 1.15f, 1.30f)
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    var authToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding   = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authToken = intent.getStringExtra(EXTRA_TOKEN) ?: ""
        setSupportActionBar(binding.toolbar)

        val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController
        appBarConfig  = AppBarConfiguration(setOf(
            R.id.homeFragment, R.id.noticeboardFragment,
            R.id.dependantsFragment, R.id.livelinessFragment, R.id.verificationFragment
        ))
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNav.setupWithNavController(navController)
        setupFontControls()
    }

    private fun setupFontControls() {
        val currentScale = FontPrefs.getScale(this)
        val currentIdx   = FONT_SCALES.indexOfFirst { it == currentScale }.takeIf { it >= 0 } ?: 1
        val isBold       = FontPrefs.isBold(this)

        val sizeButtons  = listOf(binding.btnFontS, binding.btnFontM, binding.btnFontL, binding.btnFontXl)
        sizeButtons.forEachIndexed { i, btn -> btn.isSelected = (i == currentIdx) }
        binding.btnFontBold.isSelected = isBold

        sizeButtons.forEachIndexed { i, btn ->
            btn.setOnClickListener { FontPrefs.setScale(this, FONT_SCALES[i]); recreate() }
        }
        binding.btnFontBold.setOnClickListener { FontPrefs.setBold(this, !FontPrefs.isBold(this)); recreate() }
    }

    override fun onSupportNavigateUp() = navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = FontPrefs.getScale(newBase)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
