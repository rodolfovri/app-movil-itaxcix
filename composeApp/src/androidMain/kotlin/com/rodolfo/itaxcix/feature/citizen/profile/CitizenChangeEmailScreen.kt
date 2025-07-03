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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.citizen.profile.citizenProfileViewModel.CitizenContactViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenChangeEmailScreen(
    viewModel: CitizenContactViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {}
) {
    // Estados
    val newEmail by viewModel.newEmail.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val codeError by viewModel.codeError.collectAsState()

    val changeEmailState by viewModel.changeEmailState.collectAsState()
    val verifyEmailState by viewModel.verifyEmailState.collectAsState()

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }
    var showVerificationStep by remember { mutableStateOf(false) }
    var isRedirecting by remember { mutableStateOf(false) }

    // Efectos para manejar estados
    LaunchedEffect(changeEmailState) {
        when (val state = changeEmailState) {
            is CitizenContactViewModel.ChangeEmailState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onChangeEmailErrorShown()
            }
            is CitizenContactViewModel.ChangeEmailState.Success -> {
                isSuccessSnackbar = true
                showVerificationStep = true
                viewModel.onChangeEmailSuccessShown()
            }
            else -> {}
        }
    }

    LaunchedEffect(verifyEmailState) {
        when (val state = verifyEmailState) {
            is CitizenContactViewModel.VerifyEmailState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onVerifyEmailErrorShown()
            }
            is CitizenContactViewModel.VerifyEmailState.Success -> {
                isSuccessSnackbar = true
                delay(3000)
                isRedirecting = true // Indicar que se está redirigiendo
                delay(2000)
                onBackPressed()
                viewModel.onVerifyEmailSuccessShown()
            }
            else -> {}
        }
    }

    val isLoading = changeEmailState is CitizenContactViewModel.ChangeEmailState.Loading ||
            verifyEmailState is CitizenContactViewModel.VerifyEmailState.Loading
    val isSuccess = verifyEmailState is CitizenContactViewModel.VerifyEmailState.Success

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
                            text = "Cambiar Email"
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
                        // Paso 1: Solicitar nuevo email
                        Text(
                            text = "Nuevo email",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = "Ingresa tu nuevo correo electrónico. Te enviaremos un código de verificación para confirmar el cambio.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { viewModel.updateNewEmail(it) },
                            label = { Text("Nuevo correo electrónico") },
                            isError = emailError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp),
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

                        if (emailError != null) {
                            Text(
                                text = emailError ?: "",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, start = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.requestChangeEmail()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ITaxCixPaletaColors.Blue1,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Enviar código de verificación",
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                    } else {
                        // Paso 2: Verificar código
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = ITaxCixPaletaColors.Blue1,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Verificación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Text(
                            text = "Hemos enviado un código de 6 dígitos a $newEmail. Ingresa el código para confirmar el cambio.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = verificationCode,
                            onValueChange = { viewModel.updateVerificationCode(it) },
                            label = { Text("Código de verificación") },
                            isError = codeError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp),
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

                        if (codeError != null) {
                            Text(
                                text = codeError ?: "",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, start = 4.dp)
                            )
                        }

                        Column {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.verifyEmailChange()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ITaxCixPaletaColors.Blue1,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Verificar código",
                                    style = MaterialTheme.typography.labelLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    showVerificationStep = false
                                    viewModel.updateVerificationCode("")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = ITaxCixPaletaColors.Blue1
                                )
                            ) {
                                Text(
                                    text = "Cambiar email",
                                    style = MaterialTheme.typography.labelLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(8.dp)
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
                changeEmailState is CitizenContactViewModel.ChangeEmailState.Loading -> "Enviando código"
                verifyEmailState is CitizenContactViewModel.VerifyEmailState.Loading -> "Verificando código"
                isRedirecting -> "Redirigiendo"
                else -> "Verificando código"
            },
            successTitle = if (isRedirecting) "Redirigiendo" else "Email actualizado",
            loadingMessage = "Por favor espera un momento...",
            successMessage = if (isRedirecting) "Regresando a la pantalla anterior..." else "Tu email ha sido actualizado correctamente"
        )
    }
}