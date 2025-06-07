package com.rodolfo.itaxcix.feature.driver.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.driver.viewModel.DriverHomeViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixPermissionDialog
import kotlinx.coroutines.launch

@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val driverHomeState by viewModel.driverHomeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Comprobación de permisos
    val context = LocalContext.current
    val hasLocationPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Solicitud de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission.value = isGranted
        if (isGranted) {
            showConfirmDialog = true
        } else {
            // Mostrar mensaje informando que no se puede usar la función sin permisos
            isSuccessSnackbar = false
            showPermissionDialog = false
            scope.launch {
                snackbarHostState.showSnackbar("Se requiere permiso de ubicación para verificar estado de TUC")
            }
        }
    }


    LaunchedEffect(driverHomeState) {
        when (driverHomeState) {
            is DriverHomeViewModel.DriverHomeUiState.Success -> {
                isSuccessSnackbar = true
                val message = (driverHomeState as DriverHomeViewModel.DriverHomeUiState.Success).userData.message
                snackbarHostState.showSnackbar(message)
            }
            is DriverHomeViewModel.DriverHomeUiState.Error -> {
                isSuccessSnackbar = false
                val errorMessage = (driverHomeState as DriverHomeViewModel.DriverHomeUiState.Error).message
                snackbarHostState.showSnackbar(errorMessage)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (isSuccessSnackbar) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                    contentColor = Color.White,
                    dismissAction = {
                        IconButton(onClick = { data.dismiss() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSuccessSnackbar) Icons.Default.Check else Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = data.visuals.message)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Bienvenido, ${userData?.firstName ?: "Conductor"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ITaxCixPaletaColors.Blue1
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estado de TUC activa
                    Text(
                        text = "Estado TUC: ${if (userData?.isTucActive == true) "Activa" else "No activa"}",
                        fontSize = 18.sp,
                        color = if (userData?.isTucActive == true)
                            ITaxCixPaletaColors.Blue1
                        else
                            Color.Gray
                    )

                    // Última actualización (opcional)
                    userData?.lastDriverStatusUpdate?.let { lastUpdate ->
                        Text(
                            text = "Última verificación: $lastUpdate",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    // Estado de permisos de ubicación
                    Text(
                        text = "Permiso de ubicación: ${if (hasLocationPermission.value) "Concedido" else "No concedido"}",
                        fontSize = 14.sp,
                        color = if (hasLocationPermission.value) Color.Green else Color.Red
                    )
                }

                // Botón para consultar estado de TUC
                Button(
                    onClick = {
                        if (hasLocationPermission.value) {
                            showConfirmDialog = true
                        } else {
                            showPermissionDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ITaxCixPaletaColors.Blue1,
                    )
                ) {
                    Text(
                        text = "Verificar estado de TUC",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    // En DriverHomeScreen.kt
    ITaxCixPermissionDialog(
        showDialog = showPermissionDialog,
        onDismiss = { showPermissionDialog = false },
        onConfirm = {
            showPermissionDialog = false // Cerrar el diálogo inmediatamente
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        },
        permissionTitle = "Permiso requerido",
        permissionDescription = "Para verificar el estado de TUC, necesitamos acceder a tu ubicación.",
        permissionReason = "Esto es necesario para confirmar tu posición actual al conectarte con el sistema.",
        permissionIcon = Icons.Default.LocationOn,
        confirmButtonText = "Permitir ubicación"
    )       

    // Diálogo de confirmación para verificar TUC
    ITaxCixConfirmDialog(
        showDialog = showConfirmDialog,
        onDismiss = { showConfirmDialog = false },
        onConfirm = {
            viewModel.toggleDriverAvailability()
            showConfirmDialog = false
        },
        title = "Verificar estado de TUC",
        message = "¿Desea consultar si tiene una TUC activa en este momento?",
        confirmButtonColor = ITaxCixPaletaColors.Blue2
    )
}