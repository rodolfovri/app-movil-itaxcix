package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.feature.auth.viewmodel.VerifyCodeRegisterViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay


@Preview
@Composable
fun VerifyCodeRegisterScreenPreview() {
    VerifyCodeRegisterScreen(
        onBackClick = {},
        onVerifySuccess = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCodeRegisterScreen(
    viewModel: VerifyCodeRegisterViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVerifySuccess: () -> Unit,
) {

    val verifyCodeRegisterState by viewModel.verifyCodeRegisterState.collectAsState()
    val code by viewModel.code.collectAsState()
    val codeError by viewModel.codeError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    val isSuccess = viewModel.verifyCodeRegisterState.collectAsState().value is VerifyCodeRegisterViewModel.VerifyCodeRegisterState.Success
    val isLoading = viewModel.verifyCodeRegisterState.collectAsState().value is VerifyCodeRegisterViewModel.VerifyCodeRegisterState.Loading

    val timeLeftState = remember { mutableIntStateOf(5 * 60) }

    LaunchedEffect(key1 = verifyCodeRegisterState) {
        when (val state = verifyCodeRegisterState) {
            is VerifyCodeRegisterViewModel.VerifyCodeRegisterState.Success -> {
                isSuccessSnackbar = true
                delay(2000)
                onVerifySuccess()
                viewModel.onSuccessShown()
            }
            is VerifyCodeRegisterViewModel.VerifyCodeRegisterState.ResendCodeSuccess -> {
                isSuccessSnackbar = true
                snackbarHostState.showSnackbar(
                    message = "Código reenviado exitosamente. Puedes verificar tu correo o SMS.",
                    duration = SnackbarDuration.Short
                )
                viewModel.onResendCodeSuccessShown()
            }
            is VerifyCodeRegisterViewModel.VerifyCodeRegisterState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onErrorShown()
            }
            else -> { }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(padding)
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.register_verify_code),
                        contentDescription = "Recuperar contraseña",
                        modifier = Modifier
                            .fillMaxWidth(0.80f)
                            .padding(bottom = 20.dp)
                    )

                    Text(
                        text = "¡Último paso para completar tu registro!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Te hemos enviado un código de verificación a tu correo o SMS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            if (it.length <= 6) {
                                viewModel.updateCode(it)
                            }
                        },
                        label = { Text("Ingresa el código") },
                        isError = codeError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        singleLine = true,
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

                    SendCode(
                        viewModel = viewModel,
                        timeLeft = timeLeftState
                    )

                }

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
                        text = "Verificar código",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }

        // Uso del componente ITaxCixProgressRequest para manejar estados de carga y éxito
        ITaxCixProgressRequest(
            isVisible = isLoading || isSuccess,
            isSuccess = isSuccess,
            loadingTitle = "Verificando código",
            successTitle = "Verificación exitosa",
            loadingMessage = "Estamos validando tu código...",
            successMessage = "¡Tu cuenta ha sido activada! Redirigiendo..."
        )
    }
}

@Composable
fun SendCode(
    viewModel: VerifyCodeRegisterViewModel,
    timeLeft: MutableState<Int>
) {
    val enabled = timeLeft.value <= 0
    val resendCodeState = viewModel.verifyCodeRegisterState.collectAsState().value

    // Temporizador
    LaunchedEffect(Unit) {
        while (timeLeft.value > 0) {
            delay(1000L)
            timeLeft.value -= 1
        }
    }

    // Resetear el contador cuando se reenvía el código exitosamente
    LaunchedEffect(resendCodeState) {
        if (resendCodeState is VerifyCodeRegisterViewModel.VerifyCodeRegisterState.ResendCodeSuccess) {
            timeLeft.value = 5 * 60 // Reiniciar el contador a 5 minutos
        }
    }

    // Formato MM:SS
    val minutes = timeLeft.value / 60
    val seconds = timeLeft.value % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Text(
        text = if (enabled) "Solicitar nuevo código" else "Reenviar código en $formattedTime",
        style = MaterialTheme.typography.bodyMedium.copy(
            color = if (enabled) ITaxCixPaletaColors.Blue1 else Color.Gray,
        ),
        modifier = Modifier
            .padding(bottom = 20.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.resendCode()
            },
        textAlign = TextAlign.Center
    )
}
