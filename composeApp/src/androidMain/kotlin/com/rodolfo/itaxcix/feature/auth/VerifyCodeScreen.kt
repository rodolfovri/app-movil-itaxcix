package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.VerifyCodeViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@Preview
@Composable
fun VerifyCodeScreenPreview() {
    VerifyCodeScreen(
        onVerifyCodeSuccess = { userId, token ->
            // Acción de éxito de verificación de código
        }
    )
}

@Composable
fun VerifyCodeScreen(
    viewModel: VerifyCodeViewModel = hiltViewModel(),
    onVerifyCodeSuccess: (userId: Int, token: String) -> Unit,
) {

    val verifyCodeState by viewModel.verifyCodeState.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val code by viewModel.code.collectAsState()
    val codeError by viewModel.codeError.collectAsState()
    val contact by viewModel.contact.collectAsState()
    val contactType by viewModel.contactTypeId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    // Estados para controlar la interfaz de carga y redirección
    val isLoading = verifyCodeState is VerifyCodeViewModel.VerifyCodeState.Loading
    val isRedirecting = verifyCodeState is VerifyCodeViewModel.VerifyCodeState.Success

    LaunchedEffect(key1 = verifyCodeState) {
        when (val state = verifyCodeState) {
            is VerifyCodeViewModel.VerifyCodeState.Success -> {
                // Mostrar un Snackbar de éxito
                isSuccessSnackbar = true
                delay(2000)
                onVerifyCodeSuccess(userId, state.response.token)
                viewModel.onSuccessShown()
            }
            is VerifyCodeViewModel.VerifyCodeState.Error -> {
                // Mostrar un Snackbar de error
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onErrorShown()
            }
            else -> {
            }
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
            containerColor = Color.White
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
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¡Casi listo para volver!",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        val mensaje = if (contactType == 1) {
                            "Te hemos enviado un código de verificación a tu correo: $contact"
                        } else {
                            "Te hemos enviado un código de verificación a tu número: $contact"
                        }

                        Text(
                            text = mensaje,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 30.dp),
                        )

                        OutlinedTextField(
                            value = code,
                            onValueChange = {
                                if (it.length <= 6) {
                                    viewModel.updateCode(it)
                                }
                            },
                            label = { Text(text = "Ingresa tu código") },
                            isError = codeError != null,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
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

                        Button(
                            onClick = { viewModel.verifyCode() },
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
                                text = "Enviar código de recuperación",
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Este código es parte del proceso de recuperación para cuentas registradas.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .align(Alignment.BottomCenter),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Overlay bloqueador de interacciones cuando está cargando o redirigiendo
        ITaxCixProgressRequest(
            isVisible = isLoading || isRedirecting,
            isSuccess = isRedirecting,
            loadingTitle = "Verificando",
            successTitle = "Código verificado",
            loadingMessage = "Comprobando el código ingresado...",
            successMessage = "Redirigiendo a restablecimiento de contraseña..."
        )
    }
}