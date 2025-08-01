package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.domain.model.LoginResult
import com.rodolfo.itaxcix.feature.auth.viewmodel.LoginViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay


@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onBackClick: () -> Unit = { },
    onDriverLoginSuccess: () -> Unit = { },
    onCitizenLoginSuccess: () -> Unit = { },
    onRegisterClick: () -> Unit = { },
    onRecoveryClick: () -> Unit = { },
    onRoleSelectionRequired: (List<Int>, List<String>, Any) -> Unit = { _, _, _ -> }
) {

    val focusManager = LocalFocusManager.current

    val loginState by viewModel.loginState.collectAsState()
    val isLoading = loginState is LoginViewModel.LoginState.Loading
    val isRedirecting = loginState is LoginViewModel.LoginState.Success

    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()

    val usernameError by viewModel.usernameError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    var isPassVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = loginState) {
        when(val state = loginState) {
            is LoginViewModel.LoginState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onErrorShown()
            }
            is LoginViewModel.LoginState.MultipleRoles -> {
                // Extraer IDs y nombres por separado
                val roleIds = state.roles.map { it.id }
                val roleNames = state.roles.map { it.name }
                onRoleSelectionRequired(roleIds, roleNames, state.user)
            }
            is LoginViewModel.LoginState.Success -> {
                isSuccessSnackbar = true
                delay(2000)
                val loginResult = state.user as LoginResult
                val userRoleIds = loginResult.data.roles.map { it.id }

                when {
                    userRoleIds.contains(2) -> onDriverLoginSuccess() // ID 2 = CONDUCTOR
                    userRoleIds.contains(1) -> onCitizenLoginSuccess() // ID 1 = CIUDADANO
                    else -> {
                        // Rol no reconocido, mostrar error
                        snackbarHostState.showSnackbar(
                            message = "Rol de usuario no reconocido",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                viewModel.onSuccessShown()
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
                                contentDescription = null,
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
                        IconButton(onClick = { onBackClick() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back),
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¡Accede a iTaxCix!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Inicia sesión para comenzar a moverte con seguridad.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { newValue ->
                            val filteredValue = newValue.filter { it.isDigit() } // Filtra solo números
                            if (filteredValue.length <= 8) { // Limita a 10 dígitos
                                viewModel.updateUsername(filteredValue)
                            }
                        },
                        label = { Text(text = "Ingresa tu número de documento") },
                        isError = usernameError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ITaxCixPaletaColors.Blue1,
                            unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                            cursorColor = ITaxCixPaletaColors.Blue1,
                            focusedLabelColor = ITaxCixPaletaColors.Blue1,
                            disabledBorderColor = ITaxCixPaletaColors.Blue3,
                            disabledLabelColor = ITaxCixPaletaColors.Blue3,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            selectionColors = TextSelectionColors(
                                handleColor = ITaxCixPaletaColors.Blue1,
                                backgroundColor = ITaxCixPaletaColors.Blue3
                            )
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text(text = "Ingresa tu contraseña") },
                        isError = passwordError != null,
                        visualTransformation = if (isPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPassVisible = !isPassVisible }) {
                                Icon(
                                    imageVector = if (isPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (isPassVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = ITaxCixPaletaColors.Blue1
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ITaxCixPaletaColors.Blue1,
                            unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                            cursorColor = ITaxCixPaletaColors.Blue1,
                            focusedLabelColor = ITaxCixPaletaColors.Blue1,
                            disabledBorderColor = ITaxCixPaletaColors.Blue3,
                            disabledLabelColor = ITaxCixPaletaColors.Blue3,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            selectionColors = TextSelectionColors(
                                handleColor = ITaxCixPaletaColors.Blue1,
                                backgroundColor = ITaxCixPaletaColors.Blue3
                            )
                        )
                    )

                    Button(
                        onClick = {
                            focusManager.clearFocus() // Oculta el teclado al hacer clic en el botón
                            viewModel.login()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ITaxCixPaletaColors.Blue1,
                            contentColor = Color.White,
                            // Añadimos colores para el estado deshabilitado iguales a los normales
                            disabledContainerColor = ITaxCixPaletaColors.Blue1,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp)
                            .clickable (
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onRecoveryClick() },
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        color = ITaxCixPaletaColors.Blue1
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        append("¿No tienes una cuenta? ")
                        withStyle(
                            style = MaterialTheme.typography.labelLarge.toSpanStyle()
                                .copy(color = ITaxCixPaletaColors.Blue1)
                        ) {
                            append("Regístrate")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clickable (
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onRegisterClick() },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Overlay bloqueador de interacciones cuando está cargando o redirigiendo
        ITaxCixProgressRequest(
            isVisible = isLoading || isRedirecting,
            isSuccess = isRedirecting,
            loadingTitle = "Procesando",
            successTitle = "Inicio de sesión exitoso",
            loadingMessage = "Por favor espera un momento...",
            successMessage = "Redirigiendo..."
        )
    }
}