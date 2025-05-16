package com.rodolfo.itaxcix.feature.driver.dashboard

import DriverDrawerContent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Preview
@Composable
fun DashboardDriverScreenPreview() {
    DashboardDriverScreen(
        onLogout = {}
    )
}

object DriverRoutes {
    const val HOME = "driverHome"
    const val PROFILE = "driverProfile"
    const val AVAILABILITY = "driverAvailability"
    const val HISTORY = "driverHistory"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardDriverScreen(onLogout: () -> Unit = {}) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DriverRoutes.HOME

    val title = when (currentRoute) {
        DriverRoutes.HOME -> "Inicio"
        DriverRoutes.PROFILE -> "Perfil"
        DriverRoutes.AVAILABILITY -> "Disponibilidad"
        DriverRoutes.HISTORY -> "Historial"
        else -> "Panel del Conductor"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DriverDrawerContent(
                currentRoute = currentRoute,
                onItemClick = { route ->
                coroutineScope.launch { drawerState.close() }
                if (route == "logout") {
                    onLogout()
                } else {
                    navController.navigate(route) {
                        popUpTo(DriverRoutes.HOME) { saveState = true } // Guardar el estado
                        launchSingleTop = true // Evitar múltiples instancias
                        restoreState = true // Restaurar el estado de la navegación
                    }
                }
            })
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
                    DriverNavHost(navController = navController)
                }
            }
        )
    }
}

@Composable
fun DriverNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = DriverRoutes.HOME
    ) {
        composable(DriverRoutes.HOME) {
            DriverHomeScreen()
        }
    }
}
