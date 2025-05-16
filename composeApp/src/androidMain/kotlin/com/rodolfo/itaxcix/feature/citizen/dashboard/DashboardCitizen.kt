package com.rodolfo.itaxcix.feature.citizen.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rodolfo.itaxcix.feature.citizen.history.CitizenHistoryScreen
import com.rodolfo.itaxcix.feature.citizen.home.CitizenHomeScreen
import com.rodolfo.itaxcix.feature.citizen.profile.CitizenProfileScreen
import kotlinx.coroutines.launch

@Preview
@Composable
fun DashboardCitizenScreenPreview() {
    DashboardCitizenScreen(
        onLogout = {}
    )
}

object CitizenRoutes {
    const val HOME = "citizenHome"
    const val PROFILE = "citizenProfile"
    const val HISTORY = "citizenHistory"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCitizenScreen(onLogout: () -> Unit = {}) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: CitizenRoutes.HOME

    val title = when (currentRoute) {
        CitizenRoutes.HOME -> "Inicio"
        CitizenRoutes.PROFILE -> "Perfil"
        CitizenRoutes.HISTORY -> "Historial"
        else -> "Panel del Ciudadano"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CitizenDrawerContent(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    coroutineScope.launch { drawerState.close() }
                    if (route == "logout") {
                        onLogout()
                    } else {
                        navController.navigate(route) {
                            popUpTo(CitizenRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        },
        scrimColor = Color.Black.copy(alpha = 0.3f)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            },
            content = { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    CitizenNavHost(navController = navController)
                }
            }
        )
    }
}

@Composable
fun CitizenNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = CitizenRoutes.HOME
    ) {
        composable(CitizenRoutes.HOME) {
            CitizenHomeScreen()
        }
        composable(CitizenRoutes.PROFILE) {
            CitizenProfileScreen()
        }
        composable(CitizenRoutes.HISTORY) {
            CitizenHistoryScreen()
        }
    }
}