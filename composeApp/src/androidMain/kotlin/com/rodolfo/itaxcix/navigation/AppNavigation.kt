package com.rodolfo.itaxcix.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rodolfo.itaxcix.data.remote.api.AppModule
import com.rodolfo.itaxcix.feature.auth.LoginScreen
import com.rodolfo.itaxcix.feature.auth.RegisterOptionsScreen
import com.rodolfo.itaxcix.feature.auth.WelcomeHomeScreen
import com.rodolfo.itaxcix.feature.citizen.RegisterCitizenScreen
import com.rodolfo.itaxcix.feature.citizen.RegisterValidationCitizenScreen
import com.rodolfo.itaxcix.feature.driver.RegisterValidationDriverScreen
import com.rodolfo.itaxcix.feature.driver.viewModel.RegisterDriverViewModel

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER_OPTIONS = "register_options"
    const val REGISTER_VALIDATION_CITIZEN = "citizen_register_validation"
    const val REGISTER_VALIDATION_DRIVER = "driver_register_validation"
    const val REGISTER_CITIZEN = "citizen_register/{documentTypeId}/{document}"
    const val REGISTER_DRIVER = "driver_register/{documentTypeId}/{document}/{plate}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {

        composable(Routes.WELCOME) {
            WelcomeHomeScreen(
                onLoginClick = { navController.navigate(Routes.LOGIN) },
                onRegisterClick = { navController.navigate(Routes.REGISTER_OPTIONS) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(Routes.REGISTER_OPTIONS) {
            RegisterOptionsScreen(
                onCitizenClick = { navController.navigate(Routes.REGISTER_VALIDATION_CITIZEN) },
                onDriverClick = { navController.navigate(Routes.REGISTER_VALIDATION_DRIVER) }
            )
        }

        composable(Routes.REGISTER_VALIDATION_CITIZEN) {
            RegisterValidationCitizenScreen(
                onRegisterClick = { documentTypeId, document ->
                    navController.navigate(
                        "citizen_register/$documentTypeId/$document"
                    ) {
                        launchSingleTop = true // Evitar múltiples instancias de la misma pantalla
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.REGISTER_CITIZEN,
            arguments = listOf(
                navArgument("documentTypeId") { type = NavType.IntType },
                navArgument("document") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val documentTypeId = backStackEntry.arguments?.getInt("documentTypeId") ?: 1
            val document = backStackEntry.arguments?.getString("document") ?: ""

            RegisterCitizenScreen(
                viewModel = viewModel(
                    factory = RegisterViewModelFactory(documentTypeId, document)
                ),
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Routes.REGISTER_VALIDATION_DRIVER) {
            RegisterValidationDriverScreen(
                onRegisterClick = { documentTypeId, document, plate ->
                    navController.navigate(
                        "driver_register/$documentTypeId/$document/$plate"
                    ) {
                        launchSingleTop = true // Evitar múltiples instancias de la misma pantalla
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.REGISTER_DRIVER,
            arguments = listOf(
                navArgument("documentTypeId") { type = NavType.IntType },
                navArgument("document") { type = NavType.StringType },
                navArgument("plate") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val documentTypeId = backStackEntry.arguments?.getInt("documentTypeId") ?: 1
            val document = backStackEntry.arguments?.getString("document") ?: ""
            val plate = backStackEntry.arguments?.getString("plate") ?: ""

            RegisterCitizenScreen(
                viewModel = viewModel(
                    factory = RegisterDriverViewModelFactory(documentTypeId, document, plate)
                ),
                onBackClick = { navController.popBackStack() },
            )
        }
        // Aquí puedes agregar más destinos de navegación según sea necesario
    }
}

class RegisterViewModelFactory(
    private val documentTypeId: Int,
    private val document: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterDriverViewModel::class.java)) {
            val registerViewModel = AppModule.provideRegisterViewModel()
            // Establecer los valores recibidos de la pantalla anterior
            registerViewModel.updateDocumentTypeId(documentTypeId)
            registerViewModel.updateDocument(document)
            return registerViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class RegisterDriverViewModelFactory(
    private val documentTypeId: Int,
    private val document: String,
    private val plate: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterDriverViewModel::class.java)) {
            val registerViewModel = AppModule.provideRegisterDriverViewModel()
            // Establecer los valores recibidos de la pantalla anterior
            registerViewModel.updateDocumentTypeId(documentTypeId)
            registerViewModel.updateDocument(document)
            registerViewModel.updateLicensePlate(plate)
            return registerViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}