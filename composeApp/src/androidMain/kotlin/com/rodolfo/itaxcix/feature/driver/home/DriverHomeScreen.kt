package com.rodolfo.itaxcix.feature.driver.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.driver.viewModel.DriverHomeViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.MyColors

@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val driverHomeState by viewModel.driverHomeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    // Estado para controlar la acción pendiente
    var pendingAction by remember { mutableStateOf({ }) }

    // Estado para controlar la visibilidad del diálogo de confirmación
    var showConfirmDialog by remember { mutableStateOf(false) }


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
                        text = "Bienvenido, ${userData?.name ?: "Conductor"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ITaxCixPaletaColors.Blue1
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estado actual del conductor (usando el estado de las preferencias)
                    Text(
                        text = "Estado actual: ${if (userData?.isDriverAvailable == true) "Disponible" else "No disponible"}",
                        fontSize = 18.sp,
                        color = if (userData?.isDriverAvailable == true)
                            ITaxCixPaletaColors.Blue1
                        else
                            Color.Gray
                    )

                    // Última actualización (opcional)
                    userData?.lastDriverStatusUpdate?.let { lastUpdate ->
                        Text(
                            text = "Última actualización: $lastUpdate",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Botón que cambia según el estado de disponibilidad guardado en preferencias
                Button(
                    onClick = {
                        if (userData?.isDriverAvailable == true) {
                            pendingAction = { viewModel.driverDeactivateAvailability() }
                        } else {
                            pendingAction = { viewModel.driverActivateAvailability() }
                        }
                        showConfirmDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userData?.isDriverAvailable == true)
                            MyColors.LightRed else ITaxCixPaletaColors.Blue1,
                    )
                ) {
                    Text(
                        text = if (userData?.isDriverAvailable == true)
                            "Desactivar disponibilidad" else "Activar disponibilidad",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    // Diálogo de confirmación
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = ITaxCixPaletaColors.Background,
            titleContentColor = ITaxCixPaletaColors.Blue1,
            textContentColor = Color.DarkGray,
            title = {
                Text(
                    text = if (userData?.isDriverAvailable == true)
                        "¿Desactivar disponibilidad?" else "¿Activar disponibilidad?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (userData?.isDriverAvailable == true)
                        "Ya no estarás disponible para recibir viajes."
                    else
                        "Estarás disponible para recibir solicitudes de viaje.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAction()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (userData?.isDriverAvailable == true)
                            ITaxCixPaletaColors.Blue2 else ITaxCixPaletaColors.Blue2,
                        contentColor = Color.White
                    )
                ) {
                    Text("Sí, confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ITaxCixPaletaColors.Blue1
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

}