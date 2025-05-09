package com.rodolfo.itaxcix.feature.citizen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringArrayResource
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.data.remote.api.AppModule
import com.rodolfo.itaxcix.feature.auth.viewmodel.RegisterViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.MyColors

@Preview
@Composable
fun RegisterCitizenScreenPreview() {
    RegisterCitizenScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCitizenScreen(
    viewModel: RegisterViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppModule.provideRegisterViewModel() as T
            }
        }
    ),
    onBackClick: () -> Unit = {},
) {

    // Recolectar estados
    val registerState by viewModel.registerState.collectAsState()
    val documentTypeId by viewModel.documentTypeId.collectAsState()
    val document by viewModel.document.collectAsState()
    val alias by viewModel.alias.collectAsState()
    val password by viewModel.password.collectAsState()
    val contactTypeId by viewModel.contactTypeId.collectAsState()
    val contact by viewModel.contact.collectAsState()

    val options = stringArrayResource(id = R.array.contact_method)
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }
    var isPassVisible by remember { mutableStateOf(false) }

    var selectedContactMethod by remember { mutableStateOf("Email") }
    val contactMethodLabel = if (selectedContactMethod == "Email") {
        "Ingresa tu correo electrónico"
    } else {
        "Ingresa tu número de teléfono"
    }

    // Recolectar estados de error
    val aliasError by viewModel.aliasError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val contactError by viewModel.contactError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Estado para controlar el color del Snackbar
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    // Efecto para mostrar Snackbar cuando hay error o éxito
    LaunchedEffect(key1 = registerState) {
        when (val state = registerState) {
            is RegisterViewModel.RegisterState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.onErrorShown()
            }
            is RegisterViewModel.RegisterState.Success -> {
                isSuccessSnackbar = true
                snackbarHostState.showSnackbar(
                    message = "Registro exitoso",
                    duration = SnackbarDuration.Short
                )
                viewModel.onSuccessShown()
            }
            else -> {}
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
                        text = "¡Tu viaje comienza aquí!",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Empieza tu experiencia de viaje con nosotros.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                    )

                    OutlinedTextField(
                        value = alias,
                        onValueChange = { viewModel.updateAlias(it) },
                        label = { Text(text = "Ingresa tu alias") },
                        isError = aliasError != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ITaxCixPaletaColors.Blue1,
                            unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                            cursorColor = ITaxCixPaletaColors.Blue1,
                            focusedLabelColor = ITaxCixPaletaColors.Blue1
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
                                    imageVector = if (isPassVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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
                            focusedLabelColor = ITaxCixPaletaColors.Blue1
                        )
                    )

                    OutlinedTextField(
                        value = contact,
                        onValueChange = {
                            viewModel.updateContact(it)
                            viewModel.updateContactTypeId(if (selectedContactMethod == "Email") 1 else 2)
                        },
                        label = { Text(contactMethodLabel) },
                        isError = contactError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedContactMethod == "Email") {
                                KeyboardType.Email
                            } else {
                                KeyboardType.Phone
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ITaxCixPaletaColors.Blue1,
                            unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                            cursorColor = ITaxCixPaletaColors.Blue1,
                            focusedLabelColor = ITaxCixPaletaColors.Blue1
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .background(color = Color.White, shape = RectangleShape)
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    color = ITaxCixPaletaColors.Blue1,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(color = Color.White, shape = RectangleShape)
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    color = ITaxCixPaletaColors.Blue1,
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.registerCitizen() },
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
                            text = "Registrarse",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }

                }
            }

            // Texto "¿Ya estás registrado?" en la parte inferior
            Text(
                text = buildAnnotatedString {
                    append("¿Ya estás registrado? ")
                    withStyle(
                        style = MaterialTheme.typography.labelLarge.toSpanStyle()
                            .copy(color = ITaxCixPaletaColors.Blue1)
                    ) {
                        append("Inicia Sesión")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .align(Alignment.BottomCenter),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            // ⬇ Esto se muestra encima de todo cuando está cargando
            if (registerState is RegisterViewModel.RegisterState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(120.dp),
                        strokeWidth = 8.dp,
                        color = ITaxCixPaletaColors.Blue1
                    )
                }
            }
        }
    }
}