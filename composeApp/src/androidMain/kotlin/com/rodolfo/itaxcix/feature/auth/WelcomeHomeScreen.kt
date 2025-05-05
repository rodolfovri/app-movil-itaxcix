package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodolfo.itaxcix.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun WelcomeHomeScreenPreview() {
    WelcomeHomeScreen(
        onLoginClick = {},
        onRegisterClick = {}
    )
}

@Composable
fun WelcomeHomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "iTaxCix",
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start
        )

        Image(
            painter = painterResource(R.drawable.ico_itaxcix_imagen_titulo),
            contentDescription = "Logo de iTaxCix",
            modifier = Modifier.fillMaxWidth().height(350.dp)
        )

        // Título
        Text(
            text = "¡Viaja seguro!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Subtítutlo
        Text(
            text = "Solicita un conductor o regístrate para ofrecer tus servicios. Todo desde un solo lugar, con confianza y rapidez.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(300.dp)
                .padding(bottom = 30.dp)
        )

        Button(
            modifier = Modifier.width(300.dp)
                .padding(bottom = 12.dp,
                    top = 20.dp),
            onClick = { onLoginClick() },
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
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp)
            )
        }

        OutlinedButton(
            modifier = Modifier.width(300.dp),
            onClick = { onRegisterClick() },
            shape = RectangleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White, // Color de fondo del botón
                contentColor = ITaxCixPaletaColors.Blue1 // Color del texto del botón
            ),
            border = BorderStroke(1.dp, ITaxCixPaletaColors.Blue1) // Color del borde del botón
        ) {
            Text(
                text = "Regístrate",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}