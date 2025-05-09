package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun WelcomeHomeScreenPreview() {
    WelcomeHomeScreen(
        onLoginClick = {},
        onRegisterClick = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeHomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "iTaxCix",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ico_itaxcix_welcome),
                    contentDescription = "Logo de iTaxCix",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Text(
                    text = "¡Viaja seguro!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Text(
                    text = "Solicita un conductor o regístrate para ofrecer tus servicios. Todo desde un solo lugar, con confianza y rapidez.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(300.dp)
                        .padding(bottom = 30.dp)
                )
            }

            // ⬇ Botones abajo de la pantalla
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier
                        .width(300.dp)
                        .padding(bottom = 12.dp),
                    onClick = { onLoginClick() },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ITaxCixPaletaColors.Blue1,
                        contentColor = Color.White
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

                OutlinedButton(
                    modifier = Modifier.width(300.dp),
                    onClick = { onRegisterClick() },
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = ITaxCixPaletaColors.Blue1
                    ),
                    border = BorderStroke(1.dp, ITaxCixPaletaColors.Blue1)
                ) {
                    Text(
                        text = "Crea tu cuenta",
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
}