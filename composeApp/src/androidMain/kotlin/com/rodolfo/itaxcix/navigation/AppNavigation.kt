package com.rodolfo.itaxcix.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rodolfo.itaxcix.data.remote.dto.websockets.InitialDriversResponse
import com.rodolfo.itaxcix.data.remote.dto.websockets.TripRequestMessage
import com.rodolfo.itaxcix.feature.auth.LoginScreen
import com.rodolfo.itaxcix.feature.auth.RecoveryScreen
import com.rodolfo.itaxcix.feature.auth.RegisterOptionsScreen
import com.rodolfo.itaxcix.feature.auth.ResetPasswordScreen
import com.rodolfo.itaxcix.feature.auth.VerifyCodeRegisterScreen
import com.rodolfo.itaxcix.feature.auth.VerifyCodeScreen
import com.rodolfo.itaxcix.feature.auth.WelcomeHomeScreen
import com.rodolfo.itaxcix.feature.auth.viewmodel.CameraValidationViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.LoginViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.RecoveryViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.RegisterViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.ResetPasswordViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.VerifyCodeRegisterViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.VerifyCodeViewModel
import com.rodolfo.itaxcix.feature.citizen.CameraValidationScreen
import com.rodolfo.itaxcix.feature.citizen.dashboard.DashboardCitizenScreen
import com.rodolfo.itaxcix.feature.citizen.RegisterCitizenScreen
import com.rodolfo.itaxcix.feature.citizen.RegisterValidationCitizenScreen
import com.rodolfo.itaxcix.feature.citizen.travel.CitizenTripScreen
import com.rodolfo.itaxcix.feature.citizen.travel.RideRequestScreen
import com.rodolfo.itaxcix.feature.citizen.travel.WaitingForDriverScreen
import com.rodolfo.itaxcix.feature.driver.CameraValidationDriverScreen
import com.rodolfo.itaxcix.feature.driver.dashboard.DashboardDriverScreen
import com.rodolfo.itaxcix.feature.driver.RegisterDriverScreen
import com.rodolfo.itaxcix.feature.driver.RegisterValidationDriverScreen
import com.rodolfo.itaxcix.feature.driver.travel.DriverTripInProgressScreen
import com.rodolfo.itaxcix.feature.driver.viewModel.AuthViewModel
import com.rodolfo.itaxcix.feature.driver.viewModel.CameraValidationDriverViewModel
import com.rodolfo.itaxcix.feature.driver.viewModel.DriverHomeViewModel
import com.rodolfo.itaxcix.feature.driver.viewModel.RegisterDriverViewModel
import com.rodolfo.itaxcix.ui.design.ITaxCixInactivityDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixInactivityHandler
import kotlinx.coroutines.launch

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val RECOVERY = "recovery"
    const val VERIFY_CODE = "verify_code/{contactTypeId}/{contact}/{userId}"
    const val RESET_PASSWORD = "reset_password/{userId}/{token}"
    const val REGISTER_OPTIONS = "register_options"
    const val REGISTER_VALIDATION_CITIZEN = "citizen_register_validation"
    const val REGISTER_VALIDATION_DRIVER = "driver_register_validation"
    const val REGISTER_CAMERA = "camera_validation/{personId}"
    const val REGISTER_CAMERA_DRIVER = "camera_validation_driver/{personId}/{vehicleId}"
    const val REGISTER_CITIZEN = "citizen_register/{personId}"
    const val REGISTER_DRIVER = "driver_register/{personId}/{vehicleId}"
    const val VERIFY_CODE_REGISTER = "verify_code_register/{userId}"
    const val DASHBOARD_CITIZEN = "dashboard_citizen"
    const val DASHBOARD_DRIVER = "dashboard_driver"

    // Parámetros para la pantalla de solicitud de viaje
    const val RIDE_REQUEST = "ride_request/{driverId}/{driverName}/{driverLat}/{driverLng}/{driverRating}"
    const val DRIVER_TRIP_IN_PROGRESS = "driver_trip_in_progress/{tripId}/{passengerId}/{originLat}/{originLng}/{destLat}/{destLng}/{passengerName}/{passengerRating}"
    const val WAITING_FOR_DRIVER = "waiting_for_driver/{tripId}/{driverName}"
    const val CITIZEN_TRIP_IN_PROGRESS = "citizen_trip_in_progress/{tripId}/{driverId}/{driverName}"
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val preferencesManager = viewModel.preferencesManager
    val userData by viewModel.preferencesManager.userData.collectAsState()
    var showInactivityDialog by remember { mutableStateOf(false) }

    // Maneja la inactividad SOLO si el usuario está logueado
    ITaxCixInactivityHandler(
        timeoutMinutes = 10,
        userData = userData,
        onInactivityTimeout = {
            showInactivityDialog = true
        }
    )

    // Diálogo de inactividad
    ITaxCixInactivityDialog(
        showDialog = showInactivityDialog,
        onDismiss = {
            showInactivityDialog = false
            navController.navigate(Routes.LOGIN) {
                popUpTo(Routes.WELCOME) { inclusive = false }
            }
            scope.launch {
                preferencesManager.clearUserData()
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {

        composable(
            Routes.WELCOME
        ) {
            WelcomeHomeScreen(
                onLoginClick = { navController.navigate(Routes.LOGIN) },
                onRegisterClick = { navController.navigate(Routes.REGISTER_OPTIONS) }
            )
        }

        composable(
            Routes.LOGIN
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val viewModel = hiltViewModel<LoginViewModel>()

            viewModel.updateUsername(username)
            viewModel.updatePassword(password)

            LoginScreen(
                viewModel = viewModel,
                onBackClick = { navController.navigate(Routes.REGISTER_OPTIONS) },
                onDriverLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD_DRIVER) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onCitizenLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD_CITIZEN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onRegisterClick = { navController.navigate(Routes.REGISTER_OPTIONS) },
                onRecoveryClick = { navController.navigate(Routes.RECOVERY) }
            )
        }

        composable(Routes.REGISTER_OPTIONS) {
            RegisterOptionsScreen(
                onCitizenClick = { navController.navigate(Routes.REGISTER_VALIDATION_CITIZEN) },
                onDriverClick = { navController.navigate(Routes.REGISTER_VALIDATION_DRIVER) }
            )
        }

        composable(Routes.REGISTER_VALIDATION_CITIZEN) {
            RegisterValidationCitizenScreen(
                onBackClick = { navController.popBackStack() },
                onCameraClick = { personId ->
                    navController.navigate(
                        "camera_validation/$personId"
                    ) {
                        launchSingleTop = true // Evitar múltiples instancias de la misma pantalla
                    }
                }
            )
        }

        composable(
            route = Routes.REGISTER_CITIZEN,
            arguments = listOf(
                navArgument("personId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 1

            val viewModel = hiltViewModel<RegisterViewModel>()
            viewModel.updatePersonId(personId)

            RegisterCitizenScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = { userId ->
                    navController.navigate("verify_code_register/${userId}") {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onLoginClick = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.REGISTER_VALIDATION_DRIVER) {
            RegisterValidationDriverScreen(
                onBackClick = { navController.popBackStack() },
                onCameraClick = { personId, vehicleId ->
                    navController.navigate(
                        "camera_validation_driver/$personId/$vehicleId"
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.REGISTER_CAMERA_DRIVER,
            arguments = listOf(
                navArgument("personId") { type = NavType.StringType },
                navArgument("vehicleId") { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getString("personId")?.toIntOrNull()
            val vehicleId = backStackEntry.arguments?.getString("vehicleId")?.toIntOrNull()

            val viewModel = hiltViewModel<CameraValidationDriverViewModel>()
            viewModel.setDocumentData(personId)
            viewModel.setVehicleId(vehicleId)

            CameraValidationDriverScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onValidationSuccess = { personId, vehicleId ->
                    navController.navigate(
                        "driver_register/$personId/$vehicleId"
                    ) {
                        popUpTo(Routes.REGISTER_VALIDATION_DRIVER) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.REGISTER_DRIVER,
            arguments = listOf(
                navArgument("personId") { type = NavType.IntType },
                navArgument("vehicleId") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 1
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: 1

            val viewModel = hiltViewModel<RegisterDriverViewModel>()
            viewModel.updatePersonId(personId)
            viewModel.updateVehicleId(vehicleId)

            RegisterDriverScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = { userId ->
                    // Navegar a la pantalla de verificación de código en lugar de login
                    navController.navigate("verify_code_register/${userId}") {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onLoginClick = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.DASHBOARD_CITIZEN) {

            val viewModel = hiltViewModel<AuthViewModel>()

            DashboardCitizenScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
                onNavigateToRideRequest = { driver ->
                    navController.navigate(
                        "ride_request/${driver.id}/${driver.fullName}/${driver.location.lat}/${driver.location.lng}/${driver.rating}"
                    )
                }
            )
        }

        composable(Routes.DASHBOARD_DRIVER) {

            val viewModel = hiltViewModel<AuthViewModel>()
            val driverViewModel = hiltViewModel<DriverHomeViewModel>()
            DashboardDriverScreen(
                viewModel = viewModel,
                driverWebSocketService = driverViewModel.driverWebSocketService,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                }
            )
        }

        composable(
            Routes.RECOVERY
        ) { backStackEntry ->
            val contactTypeId = backStackEntry.arguments?.getInt("contactTypeId") ?: 1
            val contact = backStackEntry.arguments?.getString("contact") ?: ""

            val viewModel = hiltViewModel<RecoveryViewModel>()
            viewModel.updateContactTypeId(contactTypeId)
            viewModel.updateContact(contact)

            RecoveryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onVerifyClick = { contactTypeId: Int, contact: String, userId ->
                    navController.navigate(
                        "verify_code/$contactTypeId/$contact/$userId"
                    ) {
                        launchSingleTop = true // Evitar múltiples instancias de la misma pantalla
                    }
                }
            )
        }

        composable(
            route = Routes.VERIFY_CODE,
            arguments = listOf(
                navArgument("contactTypeId") { type = NavType.IntType },
                navArgument("contact") { type = NavType.StringType },
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val contactTypeId = backStackEntry.arguments?.getInt("contactTypeId") ?: 1
            val contact = backStackEntry.arguments?.getString("contact") ?: ""
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            val viewModel = hiltViewModel<VerifyCodeViewModel>()
            viewModel.updateContactTypeId(contactTypeId)
            viewModel.updateContact(contact)
            viewModel.updateUserId(userId)

            VerifyCodeScreen(
                viewModel = viewModel,
                onVerifyCodeSuccess = { userId, token ->
                    navController.navigate("reset_password/$userId/$token") {
                        launchSingleTop = true // Evitar múltiples instancias de la misma pantalla
                    }
                },
            )
        }

        composable(
            route = Routes.RESET_PASSWORD,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("token") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val token = backStackEntry.arguments?.getString("token") ?: ""

            val viewModel = hiltViewModel<ResetPasswordViewModel>()
            viewModel.updateUserId(userId)
            viewModel.updateToken(token)

            ResetPasswordScreen(
                viewModel = viewModel,
                onResetSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                },
            )
        }

        composable(
            route = Routes.REGISTER_CAMERA,
            arguments = listOf(
                navArgument("personId") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 1

            val viewModel = hiltViewModel<CameraValidationViewModel>()
            viewModel.setDocumentData(personId)

            CameraValidationScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onValidationSuccess = { personId ->
                    // Solo usamos los dos primeros parámetros e ignoramos el File
                    navController.navigate(
                        "citizen_register/$personId"
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.VERIFY_CODE_REGISTER,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0

            val viewModel = hiltViewModel<VerifyCodeRegisterViewModel>()
            viewModel.updateUserId(userId)

            VerifyCodeRegisterScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onVerifySuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WELCOME) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Routes.RIDE_REQUEST,
            arguments = listOf(
                navArgument("driverId") { type = NavType.IntType },
                navArgument("driverName") { type = NavType.StringType },
                navArgument("driverLat") { type = NavType.FloatType },
                navArgument("driverLng") { type = NavType.FloatType },
                navArgument("driverRating") { type = NavType.FloatType },
            )
        ) { backStackEntry ->
            val driverId = backStackEntry.arguments?.getInt("driverId") ?: 0
            val driverName = backStackEntry.arguments?.getString("driverName") ?: ""
            val driverLat = backStackEntry.arguments?.getFloat("driverLat")?.toDouble() ?: 0.0
            val driverLng = backStackEntry.arguments?.getFloat("driverLng")?.toDouble() ?: 0.0
            val driverRating = backStackEntry.arguments?.getFloat("driverRating")?.toDouble() ?: 0.0

            val driver = InitialDriversResponse.DriverInfo(
                id = driverId,
                fullName = driverName,
                image = "",
                location = InitialDriversResponse.Location(lat = driverLat, lng = driverLng),
                rating = driverRating,
                timestamp = System.currentTimeMillis()
            )

            RideRequestScreen(
                driver = driver,
                onBackClick = { navController.popBackStack() },
                onNavigateToWaitingScreen = { tripId, driverName ->
                    navController.navigate(
                        "waiting_for_driver/$tripId/$driverName"
                    ) {
                        launchSingleTop = true // Evitar múltiples instancias de la misma pantalla
                    }

                }
            )
        }

        composable(
            route = Routes.DRIVER_TRIP_IN_PROGRESS,
            arguments = listOf(
                navArgument("tripId") { type = NavType.IntType },
                navArgument("passengerId") { type = NavType.IntType },
                navArgument("originLat") { type = NavType.FloatType },
                navArgument("originLng") { type = NavType.FloatType },
                navArgument("destLat") { type = NavType.FloatType },
                navArgument("destLng") { type = NavType.FloatType },
                navArgument("passengerName") { type = NavType.StringType },
                navArgument("passengerRating") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: 0
            val passengerId = backStackEntry.arguments?.getInt("passengerId") ?: 0
            val originLat = backStackEntry.arguments?.getFloat("originLat")?.toDouble() ?: 0.0
            val originLng = backStackEntry.arguments?.getFloat("originLng")?.toDouble() ?: 0.0
            val destLat = backStackEntry.arguments?.getFloat("destLat")?.toDouble() ?: 0.0
            val destLng = backStackEntry.arguments?.getFloat("destLng")?.toDouble() ?: 0.0
            val passengerName = backStackEntry.arguments?.getString("passengerName")?.replace("_", " ") ?: ""
            val passengerRating = backStackEntry.arguments?.getFloat("passengerRating")?.toDouble() ?: 0.0

            val trip = TripRequestMessage.TripRequestData(
                tripId = tripId,
                passengerId = passengerId,
                origin = TripRequestMessage.Location(lat = originLat, lng = originLng),
                destination = TripRequestMessage.Location(lat = destLat, lng = destLng),
                passengerName = passengerName,
                passengerRating = passengerRating
            )

            DriverTripInProgressScreen(
                driverTrip = trip
            )
        }

        composable(
            route = Routes.WAITING_FOR_DRIVER,
            arguments = listOf(
                navArgument("tripId") { type = NavType.IntType },
                navArgument("driverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: 0
            val driverName = backStackEntry.arguments?.getString("driverName") ?: ""

            WaitingForDriverScreen(
                tripId = tripId,
                driverName = driverName,
                onBackToHome = {
                    navController.navigate(Routes.DASHBOARD_CITIZEN) {
                        popUpTo(Routes.DASHBOARD_CITIZEN) { inclusive = true }
                    }
                },
                onTripAccepted = { tripId, driverId, driverName ->
                    navController.navigate(
                        "citizen_trip_in_progress/$tripId/$driverId/$driverName"
                    ) {
                        popUpTo(Routes.DASHBOARD_CITIZEN) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.CITIZEN_TRIP_IN_PROGRESS,
            arguments = listOf(
                navArgument("tripId") { type = NavType.IntType },
                navArgument("driverId") { type = NavType.IntType },
                navArgument("driverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getInt("tripId") ?: 0
            val driverId = backStackEntry.arguments?.getInt("driverId") ?: 0
            val driverName = backStackEntry.arguments?.getString("driverName") ?: ""

            CitizenTripScreen(
                tripId = tripId,
                driverId = driverId,
                driverName = driverName,
                onBackToHome = {
                    navController.navigate(Routes.DASHBOARD_CITIZEN) {
                        popUpTo(Routes.DASHBOARD_CITIZEN) {
                            inclusive = false
                        }
                    }
                }
            )
        }
    }
}