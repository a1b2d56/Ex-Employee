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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        // Destinations where the font/bold toolbar buttons should be hidden
        private val HIDE_FONT_BUTTONS = setOf(
            R.id.settingsFragment,
            R.id.aboutFragment
        )
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    var authToken: String = ""
    private var toolbarMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding   = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authToken = intent.getStringExtra(EXTRA_TOKEN) ?: ""
        setSupportActionBar(binding.toolbar)

        val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController

        // Top-level destinations (no back arrow)
        appBarConfig = AppBarConfiguration(setOf(
            R.id.mainTabsFragment
        ))
        setupActionBarWithNavController(navController, appBarConfig)

        // Apply edge-to-edge window insets to AppBarLayout
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, sysBars.top, 0, 0)
            insets
        }

        // Hide/show toolbar font buttons and deselect bottom nav on drawer-only screens
        // Hide/show toolbar font buttons
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val show = destination.id !in HIDE_FONT_BUTTONS
            toolbarMenu?.findItem(R.id.action_font_size)?.isVisible = show
            toolbarMenu?.findItem(R.id.action_bold)?.isVisible = show
        }

        // Drawer item clicks → navigate then close drawer
        binding.navDrawer.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            when (item.itemId) {
                R.id.homeFragment -> { switchToTab(0); true }
                R.id.noticeboardFragment -> { switchToTab(1); true }
                R.id.dependantsFragment -> { switchToTab(2); true }
                R.id.livelinessFragment -> { switchToTab(3); true }
                R.id.verificationFragment, R.id.settingsFragment, R.id.aboutFragment -> {
                    navController.navigate(item.itemId)
                    true
                }
                else -> false
            }
        }

        // Modern back press handling (replaces deprecated onBackPressed)
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        toolbarMenu = menu

        // Apply initial visibility based on current destination
        val currentDest = navController.currentDestination?.id
        val show = currentDest !in HIDE_FONT_BUTTONS
        menu.findItem(R.id.action_font_size)?.isVisible = show
        menu.findItem(R.id.action_bold)?.isVisible = show

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_font_size -> { cycleFontSize(); true }
            R.id.action_bold      -> { toggleBold(); true }
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

    private fun switchToTab(index: Int) {
        // Ensure we are on the main tabs destination
        navController.popBackStack(R.id.mainTabsFragment, false)
        val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val currentFragment = host?.childFragmentManager?.fragments?.firstOrNull()
        if (currentFragment is com.powergrid.exemployee.presentation.maintabs.MainTabsFragment) {
            currentFragment.switchToTab(index)
        } else {
            // If not currently loaded, navigate there and pass the index (can handle via args if needed)
            navController.navigate(R.id.mainTabsFragment)
        }
    }

    private fun toggleBold() {
        val wasBold = FontPrefs.isBold(this)
        FontPrefs.setBold(this, !wasBold)
        Toast.makeText(this, if (!wasBold) "Bold: ON" else "Bold: OFF", Toast.LENGTH_SHORT).show()
        recreate()
    }

    override fun onSupportNavigateUp() = navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = FontPrefs.getScale(newBase)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
