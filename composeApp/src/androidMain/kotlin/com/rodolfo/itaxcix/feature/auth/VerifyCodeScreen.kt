package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodolfo.itaxcix.data.remote.api.AppModule
import com.rodolfo.itaxcix.feature.auth.viewmodel.VerifyCodeViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun VerifyCodeScreenPreview() {
    VerifyCodeScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeScreen(
    viewModel: VerifyCodeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppModule.provideVerifyCodeViewModel() as T
            }
        }
    ),
    onVerifyCodeSuccess: (userId: String) -> Unit = {},
) {

    val verifyCodeState by viewModel.verifyCodeState.collectAsState()
    val code by viewModel.code.collectAsState()
    val codeError by viewModel.codeError.collectAsState()
    val contact by viewModel.contact.collectAsState()
    val contactType by viewModel.contactTypeId.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isSuccessSnackbar by remember { mutableStateOf(false) }


    LaunchedEffect(key1 = verifyCodeState) {
        when (val state = verifyCodeState) {
            is VerifyCodeViewModel.VerifyCodeState.Loading -> {
                // Mostrar un Snackbar de carga
                snackbarHostState.showSnackbar(
                    message = "Cargando...",
                    duration = SnackbarDuration.Indefinite
                )
            }
            is VerifyCodeViewModel.VerifyCodeState.Success -> {
                // Mostrar un Snackbar de éxito
                isSuccessSnackbar = true
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                onVerifyCodeSuccess(state.userId)
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
                        text = "¡Casi listo para volver!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Mensaje personalizado según el tipo de contacto
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
                        onClick = { viewModel.verifyCode()  },
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
}