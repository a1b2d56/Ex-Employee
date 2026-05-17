package com.powergrid.exemployee.presentation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
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
        val FONT_LABELS = arrayOf("S", "M", "L", "XL")
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

        // Top-level destinations: bottom nav tabs (not the drawer item)
        appBarConfig = AppBarConfiguration(setOf(
            R.id.homeFragment, R.id.noticeboardFragment,
            R.id.dependantsFragment, R.id.livelinessFragment
        ))
        setupActionBarWithNavController(navController, appBarConfig)

        // Wire only the real nav destinations (not the "More" toggle)
        binding.bottomNav.setupWithNavController(navController)

        // Intercept the "More" hamburger tap to open the drawer instead of navigating
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_drawer_toggle) {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                false // don't select this item
            } else {
                // Default nav behaviour
                val handled = navController.popBackStack(item.itemId, false)
                if (!handled) {
                    navController.navigate(item.itemId)
                }
                true
            }
        }

        // Drawer item clicks → navigate then close drawer
        binding.navDrawer.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.verificationFragment -> {
                    navController.navigate(R.id.verificationFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_font_size -> {
                cycleFontSize()
                true
            }
            R.id.action_bold -> {
                toggleBold()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cycleFontSize() {
        val currentScale = FontPrefs.getScale(this)
        val currentIdx = FONT_SCALES.indexOfFirst { it == currentScale }.takeIf { it >= 0 } ?: 1
        val nextIdx = (currentIdx + 1) % FONT_SCALES.size
        FontPrefs.setScale(this, FONT_SCALES[nextIdx])
        Toast.makeText(this, "Font: ${FONT_LABELS[nextIdx]}", Toast.LENGTH_SHORT).show()
        recreate()
    }

    private fun toggleBold() {
        val wasBold = FontPrefs.isBold(this)
        FontPrefs.setBold(this, !wasBold)
        Toast.makeText(this, if (!wasBold) "Bold: ON" else "Bold: OFF", Toast.LENGTH_SHORT).show()
        recreate()
    }

    override fun onSupportNavigateUp() = navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = FontPrefs.getScale(newBase)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
