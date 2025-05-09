package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.data.remote.api.AppModule
import com.rodolfo.itaxcix.feature.auth.viewmodel.LoginViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.RegisterViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: LoginViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppModule.provideLoginViewModel() as T
            }
        }
    )
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isPassVisible by remember { mutableStateOf(false) }


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
                    IconButton(onClick = { navController.popBackStack() }) {
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
                Text(
                    text = "¡Accede a iTaxCix!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Inicia sesión para comenzar a moverte con seguridad.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(text = "Ingresa tu usuario") },
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
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = "Ingresa tu contraseña") },
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
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
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                Text(
                    text = "¿Olvidaste tu contraseña?",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    color = ITaxCixPaletaColors.Blue1
                )
            }

            Text(
                text = buildAnnotatedString {
                    append("¿No tienes una cuenta? ")
                    withStyle(
                        style = MaterialTheme.typography.labelLarge.toSpanStyle()
                            .copy(color = ITaxCixPaletaColors.Blue1)
                    ) {
                        append("Regístrate")
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