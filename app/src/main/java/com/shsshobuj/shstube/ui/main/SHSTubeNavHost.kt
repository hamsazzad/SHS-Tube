package com.shsshobuj.shstube.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.shsshobuj.shstube.ui.browser.BrowserScreen
import com.shsshobuj.shstube.ui.home.HomeScreen
import com.shsshobuj.shstube.ui.library.LibraryScreen
import com.shsshobuj.shstube.ui.settings.SettingsScreen
import com.shsshobuj.shstube.ui.theme.CopperOrange
import com.shsshobuj.shstube.ui.theme.MetallicBlue
import com.shsshobuj.shstube.ui.theme.ObsidianBlack
import com.shsshobuj.shstube.ui.theme.SurfaceDark

data class BottomNavItem(
    val tab: BottomNavTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(BottomNavTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(BottomNavTab.BROWSER, "Browser", Icons.Filled.Language, Icons.Outlined.Language),
    BottomNavItem(BottomNavTab.LIBRARY, "Library", Icons.Filled.Download, Icons.Outlined.Download),
    BottomNavItem(BottomNavTab.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun SHSTubeNavHost(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        bottomBar = {
            SHSTubeBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                BottomNavTab.HOME -> HomeScreen(
                    onUrlClick = { url ->
                        viewModel.updateBrowserUrl(url)
                        viewModel.selectTab(BottomNavTab.BROWSER)
                    }
                )
                BottomNavTab.BROWSER -> BrowserScreen(viewModel = viewModel)
                BottomNavTab.LIBRARY -> LibraryScreen()
                BottomNavTab.SETTINGS -> SettingsScreen()
            }
        }
    }
}

@Composable
fun SHSTubeBottomNav(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceDark,
        contentColor = CopperOrange
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = selectedTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CopperOrange,
                    selectedTextColor = CopperOrange,
                    unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF8A8A8A),
                    unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF8A8A8A),
                    indicatorColor = MetallicBlue.copy(alpha = 0.2f)
                )
            )
        }
    }
}
