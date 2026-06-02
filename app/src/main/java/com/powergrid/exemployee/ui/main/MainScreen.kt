package com.powergrid.exemployee.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.powergrid.exemployee.common.FontPrefs
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.powergrid.exemployee.ui.theme.dynamic
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.powergrid.exemployee.R
import com.powergrid.exemployee.ui.navigation.AppNavHost
import com.powergrid.exemployee.ui.navigation.Screen
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import coil.compose.AsyncImage
import androidx.compose.material.icons.outlined.Person
import com.powergrid.exemployee.ui.theme.dynamic
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powergrid.exemployee.common.UiState
import com.powergrid.exemployee.ui.home.HomeViewModel
import com.powergrid.exemployee.ui.components.SignOutDialog
import com.powergrid.exemployee.ui.components.ProfileAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authToken: String,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    
    // Lift the PagerState for the 4 main bottom nav screens
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    var isMenuOpen by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                val title = if (currentRoute == Screen.Home.route) {
                    when (pagerState.currentPage) {
                        0 -> "My Profile"
                        1 -> "Noticeboard"
                        2 -> "Dependants"
                        3 -> "Liveliness"
                        else -> "My Profile"
                    }
                } else {
                    when (currentRoute) {
                        Screen.Settings.route -> "Settings"
                        Screen.About.route -> "About"
                        else -> "Ex-Employee"
                    }
                }
                
                val isSubDestination = currentRoute in listOf(
                    Screen.Settings.route,
                    Screen.About.route
                )
                
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold.dynamic()
                        ) 
                    },
                    navigationIcon = {
                        if (isSubDestination) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentRoute != Screen.Settings.route && currentRoute != Screen.About.route) {
                            val context = LocalContext.current
                            val isBold = com.powergrid.exemployee.ui.theme.LocalIsBold.current
                            val fontScales = listOf(0.85f, 1.0f, 1.15f, 1.3f)
                            
                            IconButton(onClick = {
                                val currentScale = FontPrefs.getScale(context)
                                val nextIdx = (fontScales.indexOf(currentScale).coerceAtLeast(0) + 1) % fontScales.size
                                FontPrefs.setScale(context, fontScales[nextIdx])
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_font_size),
                                    contentDescription = "Toggle Font Size",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            IconButton(onClick = {
                                FontPrefs.setBold(context, !isBold)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_format_bold),
                                    contentDescription = "Toggle Bold Text",
                                    modifier = Modifier.size(24.dp),
                                    tint = if (isBold) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                // Telegram-style floating bottom capsule
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                        .navigationBarsPadding(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val bottomItems = listOf(
                            Triple(Screen.Home, "Home", R.drawable.ic_nav_home) to 0,
                            Triple(Screen.Home, "Notices", R.drawable.ic_nav_noticeboard) to 1,
                            Triple(Screen.Home, "Dependants", R.drawable.ic_nav_dependants) to 2,
                            Triple(Screen.Home, "Liveliness", R.drawable.ic_nav_liveliness) to 3,
                        )
                        
                        bottomItems.forEach { entry ->
                            val (itemData, targetPage) = entry
                            val (screen, label, iconResId) = itemData
                            val isSelected = !isMenuOpen && currentRoute == Screen.Home.route && pagerState.currentPage == targetPage
                            
                            FloatingNavItem(
                                selected = isSelected,
                                label = label,
                                iconRes = iconResId,
                                onClick = {
                                    isMenuOpen = false
                                    if (currentRoute != Screen.Home.route) {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    scope.launch { pagerState.animateScrollToPage(targetPage) }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 5th Navigation Item: Menu Overlay Trigger
                        FloatingNavItem(
                            selected = isMenuOpen,
                            label = "Menu",
                            iconRes = R.drawable.ic_nav_menu,
                            onClick = {
                                isMenuOpen = !isMenuOpen
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        ) { padding ->
            AppNavHost(
                navController = navController,
                pagerState = pagerState,
                authToken = authToken,
                onSignOut = onSignOut,
                modifier = Modifier.padding(padding)
            )
        }

        // Full Screen Menu Overlay
        AnimatedVisibility(
            visible = isMenuOpen,
            enter = fadeIn(animationSpec = spring()) + scaleIn(initialScale = 0.95f),
            exit = fadeOut(animationSpec = spring()) + scaleOut(targetScale = 0.95f)
        ) {
            FullScreenMenuOverlay(
                navController = navController,
                pagerState = pagerState,
                scope = scope,
                onClose = { isMenuOpen = false },
                onSignOutClick = {
                    showSignOutDialog = true
                },
                authToken = authToken
            )
        }

        if (showSignOutDialog) {
            SignOutDialog(
                onDismiss = { showSignOutDialog = false },
                onConfirm = {
                    showSignOutDialog = false
                    isMenuOpen = false
                    onSignOut()
                }
            )
        }
    }
}

@Composable
private fun FloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = selected, label = "NavItemTransition")
    val tint by transition.animateColor(label = "iconTint") { isSelected ->
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    }
    val scale by transition.animateFloat(label = "iconScale") { isSelected ->
        if (isSelected) 1.12f else 1.0f
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold.dynamic() else FontWeight.Medium.dynamic(),
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FullScreenMenuOverlay(
    navController: NavHostController,
    pagerState: PagerState,
    scope: CoroutineScope,
    onClose: () -> Unit,
    onSignOutClick: () -> Unit,
    authToken: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Fetch profile details when the overlay opens
    LaunchedEffect(authToken) {
        viewModel.loadEmployee(authToken)
    }

    val employeeState by viewModel.employee.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BackHandler(onBack = onClose)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            // Scrollable Sidebar content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Profile Header (Coil AsyncImage + Circular Backdrop)
                item {
                    val employee = (employeeState as? UiState.Success)?.data
                    val photoUrl = employee?.photoUrl
                    val name = employee?.name ?: "Retired Employee"
                    val employeeId = employee?.employeeId ?: "PG-XXXX"

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        // Circular avatar with subtle drop shadow and border
                        ProfileAvatar(
                            photoUrl = photoUrl,
                            size = 88.dp,
                            shadowElevation = 6.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "ID: $employeeId",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // Core Navigation Items Grouped into WhatsApp/Telegram Style Cards
                item {
                    val primaryColor = MaterialTheme.colorScheme.primary

                    // Navigation Helper for Pager items
                    fun navigateToHomePager(page: Int) {
                        onClose()
                        if (navController.currentBackStackEntry?.destination?.route != Screen.Home.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        scope.launch { pagerState.animateScrollToPage(page) }
                    }

                    // Navigation Helper for sub-screens
                    fun navigateToRoute(route: String) {
                        onClose()
                        if (navController.currentBackStackEntry?.destination?.route != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Core Portal Directory (including Verification Status)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column {
                                SidebarRowItem(
                                    title = "My Profile",
                                    subtitle = "View personal details",
                                    iconRes = R.drawable.ic_nav_home,
                                    onClick = { navigateToHomePager(0) },
                                    iconColor = primaryColor
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                SidebarRowItem(
                                    title = "Noticeboard",
                                    subtitle = "Read latest notifications",
                                    iconRes = R.drawable.ic_nav_noticeboard,
                                    onClick = { navigateToHomePager(1) },
                                    iconColor = primaryColor
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                SidebarRowItem(
                                    title = "Dependants",
                                    subtitle = "Manage family & dependants",
                                    iconRes = R.drawable.ic_nav_dependants,
                                    onClick = { navigateToHomePager(2) },
                                    iconColor = primaryColor
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                SidebarRowItem(
                                    title = "Liveliness Check",
                                    subtitle = "Verification & vital check",
                                    iconRes = R.drawable.ic_nav_liveliness,
                                    onClick = { navigateToHomePager(3) },
                                    iconColor = primaryColor
                                )

                            }
                        }

                        // Card 2: Utilities & App Config
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column {
                                SidebarRowItem(
                                    title = "Settings",
                                    subtitle = "Appearance & theme options",
                                    iconRes = R.drawable.ic_settings,
                                    onClick = { navigateToRoute(Screen.Settings.route) },
                                    iconColor = primaryColor
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                SidebarRowItem(
                                    title = "About",
                                    subtitle = "App information",
                                    iconRes = R.drawable.ic_info,
                                    onClick = { navigateToRoute(Screen.About.route) },
                                    iconColor = primaryColor
                                )
                            }
                        }

                        // Card 3: Session Security
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            SidebarRowItem(
                                title = "Sign Out",
                                subtitle = "Exit your current session",
                                iconRes = R.drawable.ic_logout,
                                onClick = onSignOutClick,
                                iconColor = MaterialTheme.colorScheme.error,
                                titleColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarRowItem(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded icon background container (similar to WhatsApp/Telegram Settings styles)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold.dynamic(),
                color = titleColor
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}
