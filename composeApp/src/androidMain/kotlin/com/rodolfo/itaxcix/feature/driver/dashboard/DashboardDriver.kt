package com.rodolfo.itaxcix.feature.driver.dashboard

import DriverDrawerContent
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rodolfo.itaxcix.feature.driver.history.DriverHistoryScreen
import com.rodolfo.itaxcix.feature.driver.home.DriverHomeScreen
import com.rodolfo.itaxcix.feature.driver.profile.DriverProfileScreen
import com.rodolfo.itaxcix.feature.driver.viewModel.AuthViewModel
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
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
    const val HISTORY = "driverHistory"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardDriverScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DriverRoutes.HOME

    val authState by viewModel.logoutState.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    val title = when (currentRoute) {
        DriverRoutes.HOME -> "Inicio"
        DriverRoutes.PROFILE -> "Perfil"
        DriverRoutes.HISTORY -> "Historial"
        else -> "Panel del Conductor"
    }

    LaunchedEffect(key1 = authState) {
        when(val state = authState) {
            is AuthViewModel.LogoutState.Success -> {
                onLogout()
                viewModel.onSuccessShown()
            }
            else -> {}
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DriverDrawerContent(
                currentRoute = currentRoute,
                onItemClick = { route ->
                coroutineScope.launch { drawerState.close() }
                if (route == "logout") {
                    showAuthDialog = true
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    )
                )
            },
            content = { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    DriverNavHost(navController = navController)
                }
            }
        )
    }

    // Diálogo de confirmación para cerrar sesión
    ITaxCixConfirmDialog(
        showDialog = showAuthDialog,
        onDismiss = { showAuthDialog = false },
        onConfirm = { viewModel.logout() },
        title = "Cerrar sesión",
        message = "¿Estás seguro que deseas cerrar sesión en iTaxCix?",
        confirmButtonText = "Sí, confirmar",
        dismissButtonText = "Cancelar"
    )
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
        composable(DriverRoutes.PROFILE) {
            DriverProfileScreen()
        }
        composable(DriverRoutes.HISTORY) {
            DriverHistoryScreen()
        }
    }
}
