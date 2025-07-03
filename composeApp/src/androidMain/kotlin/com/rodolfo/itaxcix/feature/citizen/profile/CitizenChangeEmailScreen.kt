package com.rodolfo.itaxcix.feature.citizen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
    onBackPressed: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    // Estados de validación
    val newEmail by viewModel.newEmail.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val codeError by viewModel.codeError.collectAsState()

    // Estados de operación
    val changeEmailState by viewModel.changeEmailState.collectAsState()
    val verifyEmailState by viewModel.verifyEmailState.collectAsState()

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }
    var showVerificationStep by remember { mutableStateOf(false) }

    // Estados de carga y éxito
    val isLoadingRequest = changeEmailState is CitizenContactViewModel.ChangeEmailState.Loading
    val isLoadingVerify = verifyEmailState is CitizenContactViewModel.VerifyEmailState.Loading
    val isSuccess = verifyEmailState is CitizenContactViewModel.VerifyEmailState.Success

    // Efectos
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
                delay(2000)
                onSuccess()
                viewModel.onVerifyEmailSuccessShown()
            }
            else -> {}
        }
    }

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
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBackPressed() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Volver atrás",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                    verticalArrangement = if (!showVerificationStep) Arrangement.SpaceBetween else Arrangement.Top
                ) {
                    if (!showVerificationStep) {
                        // Paso 1: Cambiar email
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Cambiar email",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Ingresa tu nuevo correo electrónico. Te enviaremos un código de verificación para confirmar el cambio.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 30.dp),
                            )

                            OutlinedTextField(
                                value = newEmail,
                                onValueChange = { viewModel.onNewEmailChange(it) },
                                label = { Text(text = "Nuevo correo electrónico") },
                                isError = emailError != null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email
                                ),
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
                                        .padding(bottom = 8.dp, start = 4.dp)
                                )
                            }

                            Text(
                                text = "• Asegúrate de ingresar un correo válido\n• Ejemplo: usuario@correo.com",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.requestChangeEmail()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                        }

                        Text(
                            text = buildAnnotatedString {
                                append("Al continuar, se enviará un código de verificación al nuevo correo. ")
                                withStyle(
                                    style = MaterialTheme.typography.labelLarge.toSpanStyle()
                                        .copy(color = ITaxCixPaletaColors.Blue1)
                                ) {
                                    append("Verifica tu bandeja de entrada.")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Paso 2: Verificar código
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Verificar código",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Hemos enviado un código de 6 dígitos a $newEmail. Ingresa el código para confirmar el cambio.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 30.dp),
                            )

                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = { viewModel.onVerificationCodeChange(it) },
                                label = { Text(text = "Código de verificación") },
                                isError = codeError != null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
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
                                        .padding(bottom = 8.dp, start = 4.dp)
                                )
                            }

                            Text(
                                text = "• El código debe tener exactamente 6 dígitos\n• Solo se permiten números\n• Ejemplo: 123456",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.verifyEmailChange()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    showVerificationStep = false
                                    viewModel.onVerificationCodeChange("")
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

        ITaxCixProgressRequest(
            isVisible = isLoadingRequest || isLoadingVerify || isSuccess,
            isSuccess = isSuccess,
            loadingTitle = if (isLoadingRequest) "Enviando código" else "Verificando código",
            successTitle = "¡Email actualizado!",
            loadingMessage = "Por favor espera un momento...",
            successMessage = "Regresando al perfil..."
        )
    }
}