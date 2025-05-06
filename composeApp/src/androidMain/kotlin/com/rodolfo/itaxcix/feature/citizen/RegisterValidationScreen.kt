package com.rodolfo.itaxcix.feature.citizen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun RegisterValidationCitizenScreenPreview() {
    RegisterValidationCitizenScreen(
        onRegisterClick = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterValidationCitizenScreen(
    onRegisterClick: () -> Unit
) {

    val options = stringArrayResource(id = R.array.document_types)
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }
    var documentNumber by remember { mutableStateOf("") }

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
                    IconButton(onClick = {  }) {
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
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(padding)
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(R.drawable.register_validation_citizen),
                contentDescription = "Registro de ciudadano",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .padding(bottom = 20.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .padding(bottom = 5.dp)
            ) {
                TextField(
                    value = selectedOptionText,
                    onValueChange = { selectedOptionText = it },
                    readOnly = true,
                    label = { Text("Seleccione una opción") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedContainerColor = ITaxCixPaletaColors.Background,
                        unfocusedContainerColor = ITaxCixPaletaColors.Background,
                        focusedIndicatorColor = ITaxCixPaletaColors.Blue1,
                        unfocusedIndicatorColor = ITaxCixPaletaColors.Blue3,
                        focusedLabelColor = ITaxCixPaletaColors.Blue1,
                        unfocusedLabelColor = ITaxCixPaletaColors.Blue3,
                        focusedTrailingIconColor = ITaxCixPaletaColors.Blue1,
                        unfocusedTrailingIconColor = ITaxCixPaletaColors.Blue3,
                        cursorColor = ITaxCixPaletaColors.Blue1
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
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = documentNumber,
                onValueChange = { documentNumber = it },
                label = { Text(text = "Ingresa tu número de documento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ITaxCixPaletaColors.Blue1,
                    unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                    cursorColor = ITaxCixPaletaColors.Blue1,
                    focusedLabelColor = ITaxCixPaletaColors.Blue1
                )
            )

            Button(
                onClick = { onRegisterClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ITaxCixPaletaColors.Blue1, // Color de fondo del botón
                    contentColor = Color.White // Color del texto del botón
                )
            ) {
                Text(
                    text = "Ingresar",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}