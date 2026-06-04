package com.powergrid.exemployee.ui.navigation

import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.powergrid.exemployee.ui.about.AboutScreen
import com.powergrid.exemployee.ui.dependants.DependantsScreen
import com.powergrid.exemployee.ui.home.HomeScreen
import com.powergrid.exemployee.ui.liveliness.LivelinessScreen
import com.powergrid.exemployee.ui.noticeboard.NoticeboardScreen
import com.powergrid.exemployee.ui.settings.SettingsScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState

@Composable fun AppNavHost(
    navController: NavHostController,
    pagerState: PagerState,
    authToken: String,
    onSignOut: () -> Unit,
    supportsGlass: Boolean = false,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(spring()) + slideInHorizontally(spring()) { it / 4 }
        },
        exitTransition = {
            fadeOut(spring()) + slideOutHorizontally(spring()) { -it / 4 }
        },
        popEnterTransition = {
            fadeIn(spring()) + slideInHorizontally(spring()) { -it / 4 }
        },
        popExitTransition = {
            fadeOut(spring()) + slideOutHorizontally(spring()) { it / 4 }
        },
    ) {
        composable(Screen.Home.route) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> HomeScreen(authToken = authToken, supportsGlass = supportsGlass)
                    1 -> NoticeboardScreen(authToken = authToken)
                    2 -> DependantsScreen(authToken = authToken)
                    3 -> LivelinessScreen(authToken = authToken)
                }
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                authToken = authToken,
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onSignOut = onSignOut,
            )
        }

        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}
