package com.rodolfo.itaxcix.feature.auth

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodolfo.itaxcix.data.remote.api.AppModule
import com.rodolfo.itaxcix.feature.auth.viewmodel.ResetPasswordViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun ResetPasswordScreenPreview() {
    ResetPasswordScreen()
}

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel = hiltViewModel(),
    onResetSuccess: () -> Unit = {}
) {

    val resetPasswordState by viewModel.resetPasswordState.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val newPasswordError by viewModel.newPasswordError.collectAsState()

    var isPassVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = resetPasswordState) {
        when (val state = resetPasswordState) {
            is ResetPasswordViewModel.ResetPasswordState.Loading -> {
                // Mostrar un indicador de carga si es necesario
            }
            is ResetPasswordViewModel.ResetPasswordState.Success -> {
                isSuccessSnackbar = true
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onSuccessShown()
                onResetSuccess()
            }
            is ResetPasswordViewModel.ResetPasswordState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onErrorShown()
            }

            else -> {
                // Manejar otros estados si es necesario
            }
        }
    }

    // Mostrar CircularProgressIndicator mientras carga
    if (resetPasswordState is ResetPasswordViewModel.ResetPasswordState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                strokeWidth = 8.dp,
                color = ITaxCixPaletaColors.Blue1
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Personalizar el Snackbar según el estado
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
                    .fillMaxSize(), // Para que ocupe toda la altura disponible
                verticalArrangement = Arrangement.SpaceBetween // Para distribuir los elementos verticalmente
            ) {
                // Contenedor principal del formulario
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Establece tu nueva contraseña",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Elige una contraseña segura para acceder nuevamente a tu cuenta.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { viewModel.updateNewPassword(it)  },
                        label = { Text(text = "Ingresa tu nueva contraseña") },
                        isError = newPasswordError != null,
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
                        onClick = { viewModel.resetPassword()  },
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
                            text = "Actualizar contraseña",
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
                text = "Tu nueva contraseña protegerá el acceso a tu cuenta.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .align(Alignment.BottomCenter),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}