package com.rodolfo.itaxcix.feature.citizen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.rodolfo.itaxcix.feature.citizen.viewmodel.RegisterValidationViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun RegisterValidationCitizenScreenPreview() {
    RegisterValidationCitizenScreen(
        onRegisterClick = { documentTypeId, document ->
            // Aquí puedes manejar el evento de clic en el botón "Ingresar"
            // Por ejemplo, navegar a otra pantalla o mostrar un mensaje
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterValidationCitizenScreen(
    viewModel: RegisterValidationViewModel = viewModel(),
    onRegisterClick: (documentTypeId: Int, document: String) -> Unit,
    onBackClick: () -> Unit = {},
) {
    val options = stringArrayResource(id = R.array.document_types)
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    val document by viewModel.document.collectAsState()
    val documentError by viewModel.documentError.collectAsState()
    val validationState by viewModel.validationState.collectAsState()

    LaunchedEffect(validationState) {
        when (val state = validationState) {
            is RegisterValidationViewModel.ValidationState.Success -> {
                onRegisterClick(state.documentTypeId, state.document)
                viewModel.onSuccessNavigated()
            }
            else -> {}
        }
    }

    Scaffold(
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
                    painter = painterResource(R.drawable.register_validation_citizen),
                    contentDescription = "Registro de ciudadano",
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