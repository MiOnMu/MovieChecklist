package com.project.moviechecklist.ui.screens.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.project.moviechecklist.ui.navigation.Screen
import com.project.moviechecklist.ui.screens.auth.AuthViewModel

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
fun BottomNavBar(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Ukrywamy pasek na ekranie logowania
    if (currentRoute == Screen.Auth.route) {
        return
    }

    val items = listOf(
        BottomNavItem("Watched", Icons.Filled.Done, Screen.Watched.route),
        BottomNavItem("Planned", Icons.AutoMirrored.Filled.List, Screen.Planned.route),
        BottomNavItem("Search", Icons.Filled.Search, Screen.Search.route),
        BottomNavItem("Logout", Icons.AutoMirrored.Filled.Logout, onClick = {
            authViewModel.logout()
            navController.navigate(Screen.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        })
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = item.route != null && currentRoute == item.route,
                onClick = {
                    if (item.onClick != null) {
                        item.onClick.invoke()
                    } else if (item.route != null) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Watched.route) {
                                inclusive = item.route == Screen.Watched.route
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                }
            )
        }
    }
}
