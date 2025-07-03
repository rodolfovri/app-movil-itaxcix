package com.rodolfo.itaxcix.feature.driver.dashboard

import androidx.activity.compose.BackHandler
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripRequestMessage
import com.rodolfo.itaxcix.data.remote.websocket.DriverWebSocketService
import com.rodolfo.itaxcix.feature.driver.history.DriverHistoryScreen
import com.rodolfo.itaxcix.feature.driver.home.DriverHomeScreen
import com.rodolfo.itaxcix.feature.driver.profile.DriverProfileScreen
import com.rodolfo.itaxcix.feature.driver.travel.DriverTripGoingScreen
import com.rodolfo.itaxcix.feature.driver.travel.DriverTripInProgressScreen
import com.rodolfo.itaxcix.feature.driver.viewModel.AuthViewModel
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object DriverRoutes {
    const val HOME = "driverHome"
    const val PROFILE = "driverProfile"
    const val HISTORY = "driverHistory"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardDriverScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    driverWebSocketService: DriverWebSocketService,
    onLogout: () -> Unit = {},
    onNavigateToPersonalInfo: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onNavigateToChangePhone: () -> Unit = {},
    onNavigateToTravelDetail: (Int, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onNavigateToTripInProgress: (TripRequestMessage.TripRequestData) -> Unit = {},
    onNavigateToCitizenRatings: (Int, String) -> Unit = { _, _ -> }
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DriverRoutes.HOME

    val authState by viewModel.logoutState.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    val preferencesManager = viewModel.preferencesManager
    val userData by preferencesManager.userData.collectAsState()
    val userPermissions = userData?.permissions ?: emptyList()

    val title = when (currentRoute) {
        DriverRoutes.HOME -> "Inicio"
        DriverRoutes.PROFILE -> "Perfil"
        DriverRoutes.HISTORY -> "Historial"
        else -> "Panel del Conductor"
    }

    BackHandler {
        showAuthDialog = true
    }

    LaunchedEffect(key1 = authState) {
        when(val state = authState) {
            is AuthViewModel.LogoutState.Loading -> {
                isLoggingOut = true
            }
            is AuthViewModel.LogoutState.Success -> {
                delay(2000)
                onLogout()
                viewModel.onSuccessShown()
                isLoggingOut = false
            }
            is AuthViewModel.LogoutState.Error -> {
                isLoggingOut = false
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
                },
                userPermissions = userPermissions,
                userName = userData?.fullName ?: "Usuario",
                userRole = "Conductor",
                userImage = userData?.profileImage
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
                    DriverNavHost(
                        navController = navController,
                        driverWebSocketService = driverWebSocketService,
                        onNavigateToPersonalInfo = onNavigateToPersonalInfo,
                        onNavigateToChangeEmail = onNavigateToChangeEmail,
                        onNavigateToChangePhone = onNavigateToChangePhone,
                        onNavigateToTravelDetail = onNavigateToTravelDetail,
                        onNavigateToTripInProgress = onNavigateToTripInProgress,
                        onNavigateToCitizenRatings = onNavigateToCitizenRatings
                    )
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

    ITaxCixProgressRequest(
        isVisible = isLoggingOut,
        isSuccess = authState is AuthViewModel.LogoutState.Success,
        loadingTitle = "Cerrando sesión",
        successTitle = "Sesión finalizada",
        loadingMessage = "Por favor espera un momento...",
        successMessage = "Redirigiendo al inicio de sesión..."
    )
}

@Composable
fun DriverNavHost(
    navController: NavHostController,
    driverWebSocketService: DriverWebSocketService,
    onNavigateToPersonalInfo: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onNavigateToChangePhone: () -> Unit = {},
    onNavigateToTravelDetail: (Int, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onNavigateToTripInProgress: (TripRequestMessage.TripRequestData) -> Unit = {},
    onNavigateToCitizenRatings: (Int, String) -> Unit = { _, _ -> }
) {
    NavHost(
        navController = navController,
        startDestination = DriverRoutes.HOME
    ) {
        composable(DriverRoutes.HOME) {
            DriverHomeScreen(
                driverWebSocketService = driverWebSocketService,
                onNavigateToTrip = { tripData ->
                    onNavigateToTripInProgress(tripData)
                },
                onNavigateToCitizenRatings = { citizenId, citizenName ->
                    onNavigateToCitizenRatings(citizenId, citizenName)
                },
            )
        }

        composable(DriverRoutes.PROFILE) {
            DriverProfileScreen(
                onNavigateToPersonalInfo = onNavigateToPersonalInfo,
                onNavigateToChangeEmail = onNavigateToChangeEmail,
                onNavigateToChangePhone = onNavigateToChangePhone
            )
        }
        composable(DriverRoutes.HISTORY) {
            DriverHistoryScreen(
                onNavigateToTravelDetail = onNavigateToTravelDetail
            )
        }
    }
}
