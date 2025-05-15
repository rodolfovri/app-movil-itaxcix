package com.rodolfo.itaxcix.feature.driver

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.feature.driver.viewModel.RegisterValidationViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun RegisterValidationDriverScreenPreview() {
    RegisterValidationDriverScreen(
        onRegisterClick = { documentTypeId, document, plate ->
            // Aquí puedes manejar el evento de clic en el botón "Ingresar"
            // Por ejemplo, navegar a otra pantalla o mostrar un mensaje
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterValidationDriverScreen(
    viewModel: RegisterValidationViewModel = viewModel(),
    onRegisterClick: (documentTypeId: Int, document: String, plate: String) -> Unit,
    onBackClick: () -> Unit = {},
) {

    val options = stringArrayResource(id = R.array.document_types)
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    val document by viewModel.document.collectAsState()
    val plate by viewModel.plate.collectAsState()

    val documentError by viewModel.documentError.collectAsState()
    val plateError by viewModel.plateError.collectAsState()

    val validationState by viewModel.validationState.collectAsState()
    var isSuccessSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(validationState) {
        when (val state = validationState) {
            is RegisterValidationViewModel.ValidationState.Success -> {
                isSuccessSnackbar = true
                onRegisterClick(state.documentTypeId, state.document, state.plate)
                viewModel.onSuccessNavigated()
            }

            is RegisterValidationViewModel.ValidationState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = "Cerrar",
                    duration = SnackbarDuration.Short
                )
                viewModel.onErrorShown()
            }

            else -> {}
        }
    }

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
            verticalArrangement = Arrangement.SpaceBetween // Aquí el cambio clave
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.register_validation_driver),
                    contentDescription = "Registro de conductor",
                    modifier = Modifier
                        .fillMaxWidth(0.80f)
                        .clip(CircleShape)
                        .padding(bottom = 20.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.padding(bottom = 5.dp)
                ) {
                    OutlinedTextField(
                        value = selectedOptionText,
                        onValueChange = { selectedOptionText = it },
                        readOnly = true,
                        label = { Text("Seleccione una opción") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ITaxCixPaletaColors.Blue1,
                            unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                            cursorColor = ITaxCixPaletaColors.Blue1,
                            focusedLabelColor = ITaxCixPaletaColors.Blue1,
                            unfocusedLabelColor = ITaxCixPaletaColors.Blue3
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(text = selectionOption, color = ITaxCixPaletaColors.Blue1) },
                                onClick = {
                                    selectedOptionText = selectionOption
                                    expanded = false
                                    viewModel.updateDocumentType(selectedOptionText)
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = document,
                    onValueChange = { viewModel.updateDocument(it) },
                    label = { Text(text = "Ingresa tu número de documento") },
                    isError = documentError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
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

                OutlinedTextField(
                    value = plate,
                    onValueChange = { viewModel.updatePlate(it) },
                    label = { Text(text = "Ingresa tu placa") },
                    isError = plateError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
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
                onClick = { viewModel.validate() },
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
                    text = "Ingresar",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}