package com.rodolfo.itaxcix.feature.citizen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun RegisterCitizenScreenPreview() {
    RegisterCitizenScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCitizenScreen() {

    val options = stringArrayResource(id = R.array.contact_method)
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }
    var contactMethod by remember { mutableStateOf("") }

    var contactMethodLabel by remember {
        mutableStateOf(
            if (options[0] == "Email") "Ingresa tu correo electrónico"
            else "Ingresa tu número de teléfono"
        )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(30.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Add your login form here
                Text(
                    text = "¡Bienvenido!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Registrate.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
                )

                OutlinedTextField(
                    value = "",
                    onValueChange = {  },
                    label = { Text(text = "Ingresa tu alias") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    maxLines = 1,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ITaxCixPaletaColors.Blue1,
                        unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                        cursorColor = ITaxCixPaletaColors.Blue1,
                        focusedLabelColor = ITaxCixPaletaColors.Blue1
                    )
                )

                OutlinedTextField(
                    value = "",
                    onValueChange = {  },
                    label = { Text(text = "Ingresa tu contraseña") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    maxLines = 1,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ITaxCixPaletaColors.Blue1,
                        unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                        cursorColor = ITaxCixPaletaColors.Blue1,
                        focusedLabelColor = ITaxCixPaletaColors.Blue1
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier
                        .padding(bottom = 10.dp)
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
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
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
                        modifier = Modifier
                            .background(Color.White)
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(text = selectionOption, color = ITaxCixPaletaColors.Blue1) },
                                onClick = {
                                    selectedOptionText = selectionOption
                                    expanded = false

                                    contactMethodLabel = when (selectionOption) {
                                        "Email" -> "Ingresa tu correo electrónico"
                                        "Teléfono" -> "Ingresa tu número de teléfono"
                                        else -> "Ingresa tu número de documento"
                                    }
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = contactMethod,
                    onValueChange = { contactMethod = it },
                    label = { Text(text = contactMethodLabel) },
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
                    onClick = { /* Handle login click */ },
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
                        text = "Registrarse",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }

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
                    .align(Alignment.BottomCenter),
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}
