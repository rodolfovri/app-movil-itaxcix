package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.data.remote.api.AppModule
import com.rodolfo.itaxcix.feature.auth.viewmodel.RecoveryViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import kotlinx.coroutines.delay

@Preview
@Composable
fun RecoveryScreenPreview() {
    RecoveryScreen(
        onBackClick = {  },
        onVerifyClick = { contactTypeId, contact ->
            // Acción de verificación simulada
            println("Verificando contacto: $contact con tipo $contactTypeId")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onVerifyClick: (contactTypeId: Int, contact: String) -> Unit,
) {

    val recoveryState by viewModel.recoveryState.collectAsState()
    val contact by viewModel.contact.collectAsState()
    val contactError by viewModel.contactError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = recoveryState is RecoveryViewModel.RecoveryState.Loading
    val isRedirecting = recoveryState is RecoveryViewModel.RecoveryState.Success
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    var selectedContactMethod by remember { mutableStateOf("Email") }
    val contactMethodLabel = if (selectedContactMethod == "Email") {
        "Ingresa tu correo electrónico"
    } else {
        "Ingresa tu número de teléfono"
    }

    LaunchedEffect(key1 = recoveryState) {
        when(val state = recoveryState) {
            is RecoveryViewModel.RecoveryState.Loading -> {
                // El loading se manejará con el Box sobrepuesto
            }
            is RecoveryViewModel.RecoveryState.Success -> {
                isSuccessSnackbar = true
                delay(1000)
                onVerifyClick(viewModel.contactTypeId.value, viewModel.contact.value)
                viewModel.onSuccessShown()
            }
            is RecoveryViewModel.RecoveryState.Error -> {
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
                        painter = painterResource(R.drawable.recovery_user),
                        contentDescription = "Recuperar contraseña",
                        modifier = Modifier
                            .fillMaxWidth(0.80f)
                            .padding(bottom = 20.dp)
                    )

                    Text(
                        text = "Recupera tu acceso",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Ingresa tu correo o número de teléfono para enviarte un código de verificación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp) // espacio entre los dos cuadros
                    ) {
                        // Opción Email
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = ITaxCixPaletaColors.Blue3,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedContactMethod = "Email" }
                            ) {
                                RadioButton(
                                    selected = selectedContactMethod == "Email",
                                    onClick = {
                                        selectedContactMethod = "Email"
                                        viewModel.updateContactTypeId(1)
                                        viewModel.updateContact("")
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = ITaxCixPaletaColors.Blue1,
                                        unselectedColor = ITaxCixPaletaColors.Blue1
                                    )
                                )
                                Text(
                                    text = "Email",
                                    color = ITaxCixPaletaColors.Blue1
                                )
                            }
                        }

                        // Opción Teléfono
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = ITaxCixPaletaColors.Blue3,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedContactMethod = "Teléfono" }
                            ) {
                                RadioButton(
                                    selected = selectedContactMethod == "Teléfono",
                                    onClick = {
                                        selectedContactMethod = "Teléfono"
                                        viewModel.updateContactTypeId(2)
                                        viewModel.updateContact("")
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = ITaxCixPaletaColors.Blue1,
                                        unselectedColor = ITaxCixPaletaColors.Blue1
                                    )
                                )
                                Text(
                                    text = "Teléfono",
                                    color = ITaxCixPaletaColors.Blue1
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = contact,
                        onValueChange = {
                            viewModel.updateContact(it)
                            viewModel.updateContactTypeId(if (selectedContactMethod == "Email") 1 else 2)

                        },
                        label = { Text(contactMethodLabel) },
                        isError = contactError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedContactMethod == "Email") {
                                KeyboardType.Email
                            } else {
                                KeyboardType.Phone
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
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
                }

                Button(
                    onClick = { viewModel.recoverPassword() },
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

        // Overlay de carga que cubre toda la pantalla cuando está en estado Loading
        if (isLoading || isRedirecting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = true,
                        onClick = { /* Captura clics pero no hace nada */ }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = ITaxCixPaletaColors.Blue1,
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = if (isRedirecting) "Contacto verificado, redireccionando..." else "Procesando solicitud...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}