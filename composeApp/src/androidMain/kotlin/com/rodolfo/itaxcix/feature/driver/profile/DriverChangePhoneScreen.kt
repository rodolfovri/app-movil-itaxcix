package com.rodolfo.itaxcix.feature.driver.profile

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.citizen.profile.citizenProfileViewModel.CitizenContactViewModel
import com.rodolfo.itaxcix.feature.driver.profile.driverProfileViewModel.DriverContactViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverChangePhoneScreen(
    viewModel: DriverContactViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {

    // Estados de validación
    val newPhone by viewModel.newPhone.collectAsState()
    val verificationCodePhone by viewModel.verificationCodePhone.collectAsState()
    val phoneError by viewModel.phoneError.collectAsState()
    val codePhoneError by viewModel.codePhoneError.collectAsState()

    // Estados de operación
    val changePhoneState by viewModel.changePhoneState.collectAsState()
    val verifyPhoneState by viewModel.verifyPhoneState.collectAsState()

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }
    var showVerificationStep by remember { mutableStateOf(false) }

    // Estados de carga y éxito
    val isLoadingRequest = changePhoneState is DriverContactViewModel.ChangePhoneState.Loading
    val isLoadingVerify = verifyPhoneState is DriverContactViewModel.VerifyPhoneState.Loading
    val isSuccess = verifyPhoneState is DriverContactViewModel.VerifyPhoneState.Success

    // Efectos
    LaunchedEffect(changePhoneState) {
        when (val state = changePhoneState) {
            is DriverContactViewModel.ChangePhoneState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onChangePhoneErrorShown()
            }
            is DriverContactViewModel.ChangePhoneState.Success -> {
                isSuccessSnackbar = true
                showVerificationStep = true
                viewModel.onChangePhoneSuccessShown()
            }
            else -> {}
        }
    }

    LaunchedEffect(verifyPhoneState) {
        when (val state = verifyPhoneState) {
            is DriverContactViewModel.VerifyPhoneState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onVerifyPhoneErrorShown()
            }
            is DriverContactViewModel.VerifyPhoneState.Success -> {
                isSuccessSnackbar = true
                delay(2000)
                onSuccess()
                viewModel.onVerifyPhoneSuccessShown()
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
                        // Paso 1: Cambiar teléfono
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Cambiar teléfono",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Ingresa tu nuevo número de teléfono. Te enviaremos un código por SMS para confirmar el cambio.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 30.dp),
                            )

                            OutlinedTextField(
                                value = newPhone,
                                onValueChange = { viewModel.onNewPhoneChange(it) },
                                label = { Text(text = "Nuevo número de teléfono") },
                                isError = phoneError != null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone
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

                            if (phoneError != null) {
                                Text(
                                    text = phoneError ?: "",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp, start = 4.dp)
                                )
                            }

                            Text(
                                text = "• El número debe iniciar con 9\n• Debe tener exactamente 9 dígitos\n• Ejemplo: 987654321",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.requestChangePhone()
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
                                    text = "Enviar código por SMS",
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
                                append("Al continuar, se enviará un SMS con el código al nuevo número. ")
                                withStyle(
                                    style = MaterialTheme.typography.labelLarge.toSpanStyle()
                                        .copy(color = ITaxCixPaletaColors.Blue1)
                                ) {
                                    append("Mantén tu teléfono cerca.")
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
                                text = "Verificar código SMS",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Hemos enviado un código de 6 dígitos al número +51$newPhone. Ingresa el código para confirmar el cambio.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 30.dp),
                            )

                            OutlinedTextField(
                                value = verificationCodePhone,
                                onValueChange = { viewModel.onVerificationCodePhoneChange(it) },
                                label = { Text(text = "Código de verificación") },
                                isError = codePhoneError != null,
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

                            if (codePhoneError != null) {
                                Text(
                                    text = codePhoneError ?: "",
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
                                    viewModel.verifyPhoneChange()
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
                                    viewModel.onVerificationCodePhoneChange("")
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
            loadingTitle = if (isLoadingRequest) "Enviando SMS" else "Verificando código",
            successTitle = "¡Teléfono actualizado!",
            loadingMessage = "Por favor espera un momento...",
            successMessage = "Regresando al perfil..."
        )
    }
}