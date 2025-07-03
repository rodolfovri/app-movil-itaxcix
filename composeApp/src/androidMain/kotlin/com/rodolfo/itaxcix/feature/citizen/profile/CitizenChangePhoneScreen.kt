package com.rodolfo.itaxcix.feature.citizen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.citizen.profile.citizenProfileViewModel.CitizenContactViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenChangePhoneScreen(
    viewModel: CitizenContactViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {}
) {
    // Estados
    val newPhone by viewModel.newPhone.collectAsState()
    val verificationCodePhone by viewModel.verificationCodePhone.collectAsState()
    val phoneError by viewModel.phoneError.collectAsState()
    val codePhoneError by viewModel.codePhoneError.collectAsState()

    val changePhoneState by viewModel.changePhoneState.collectAsState()
    val verifyPhoneState by viewModel.verifyPhoneState.collectAsState()

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }
    var showVerificationStep by remember { mutableStateOf(false) }
    var isRedirecting by remember { mutableStateOf(false) }

    // Efectos para manejar estados
    LaunchedEffect(changePhoneState) {
        when (val state = changePhoneState) {
            is CitizenContactViewModel.ChangePhoneState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onChangePhoneErrorShown()
            }
            is CitizenContactViewModel.ChangePhoneState.Success -> {
                isSuccessSnackbar = true
                showVerificationStep = true
                viewModel.onChangePhoneSuccessShown()
            }
            else -> {}
        }
    }

    LaunchedEffect(verifyPhoneState) {
        when (val state = verifyPhoneState) {
            is CitizenContactViewModel.VerifyPhoneState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onVerifyPhoneErrorShown()
            }
            is CitizenContactViewModel.VerifyPhoneState.Success -> {
                isSuccessSnackbar = true
                delay(3000) // Mostrar "Teléfono actualizado" por 3 segundos
                isRedirecting = true // Indicar que se está redirigiendo
                delay(2000) // Mostrar "Redirigiendo..." por 2 segundos
                onBackPressed()
                viewModel.onVerifyPhoneSuccessShown()
            }
            else -> {}
        }
    }

    val isLoading = changePhoneState is CitizenContactViewModel.ChangePhoneState.Loading ||
            verifyPhoneState is CitizenContactViewModel.VerifyPhoneState.Loading
    val isSuccess = verifyPhoneState is CitizenContactViewModel.VerifyPhoneState.Success

    Box(modifier = Modifier.fillMaxSize()) {
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
                                contentDescription = if (isSuccessSnackbar) "Éxito" else "Error",
                                tint = Color.White,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = data.visuals.message)
                        }
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Cambiar Teléfono"
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver atrás"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(ITaxCixPaletaColors.Background)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!showVerificationStep) {
                        // Paso 1: Solicitar nuevo teléfono
                        Text(
                            text = "Nuevo teléfono",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Ingresa tu nuevo número de teléfono. Te enviaremos un código de verificación por SMS para confirmar el cambio.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { viewModel.updateNewPhone(it) },
                            label = { Text("Nuevo número de teléfono") },
                            placeholder = { Text("Ingresa tu número de teléfono") },
                            isError = phoneError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ITaxCixPaletaColors.Blue1,
                                unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                                cursorColor = ITaxCixPaletaColors.Blue1,
                                focusedLabelColor = ITaxCixPaletaColors.Blue1,
                                selectionColors = TextSelectionColors(
                                    handleColor = ITaxCixPaletaColors.Blue1,
                                    backgroundColor = ITaxCixPaletaColors.Blue3
                                )
                            )
                        )

                        if (phoneError != null) {
                            Text(
                                text = phoneError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.requestChangePhone()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ITaxCixPaletaColors.Blue1,
                                contentColor = Color.White
                            ),
                            enabled = newPhone.isNotBlank() && phoneError == null &&
                                    changePhoneState !is CitizenContactViewModel.ChangePhoneState.Loading
                        ) {
                            Text(
                                text = "Enviar código de verificación",
                                fontWeight = FontWeight.Medium
                            )
                        }

                    } else {
                        // Paso 2: Verificar código
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ITaxCixPaletaColors.Blue1,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Verificar código",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Hemos enviado un código de verificación al número +51${newPhone}. Ingresa el código de 6 dígitos que recibiste.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = verificationCodePhone,
                            onValueChange = { viewModel.updateVerificationCodePhone(it) },
                            label = { Text("Código de verificación") },
                            placeholder = { Text("123456") },
                            isError = codePhoneError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ITaxCixPaletaColors.Blue1,
                                unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                                cursorColor = ITaxCixPaletaColors.Blue1,
                                focusedLabelColor = ITaxCixPaletaColors.Blue1,
                                selectionColors = TextSelectionColors(
                                    handleColor = ITaxCixPaletaColors.Blue1,
                                    backgroundColor = ITaxCixPaletaColors.Blue3
                                )
                            )
                        )

                        if (codePhoneError != null) {
                            Text(
                                text = codePhoneError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        Column {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.verifyPhoneChange()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ITaxCixPaletaColors.Blue1,
                                    contentColor = Color.White
                                ),
                                enabled = verificationCodePhone.isNotBlank() && codePhoneError == null &&
                                        verifyPhoneState !is CitizenContactViewModel.VerifyPhoneState.Loading
                            ) {
                                Text(
                                    text = "Verificar y actualizar",
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    showVerificationStep = false
                                    viewModel.updateVerificationCodePhone("")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = ITaxCixPaletaColors.Blue1
                                )
                            ) {
                                Text(
                                    text = "Cambiar número",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overlay de carga
        ITaxCixProgressRequest(
            isVisible = isLoading || isSuccess || isRedirecting,
            isSuccess = isSuccess || isRedirecting,
            loadingTitle = when {
                changePhoneState is CitizenContactViewModel.ChangePhoneState.Loading -> "Enviando código"
                verifyPhoneState is CitizenContactViewModel.VerifyPhoneState.Loading -> "Verificando código"
                isRedirecting -> "Redirigiendo"
                else -> "Verificando código"
            },
            successTitle = if (isRedirecting) "Redirigiendo" else "Teléfono actualizado",
            loadingMessage = "Por favor espera un momento...",
            successMessage = if (isRedirecting) "Regresando a la pantalla anterior..." else "Tu número de teléfono ha sido actualizado correctamente"
        )
    }
}