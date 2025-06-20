package com.rodolfo.itaxcix.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Preview
@Composable
fun RegisterOptionsScreenPreview() {
    RegisterOptionsScreen(
        onCitizenClick = {},
        onDriverClick = {}
    )
}

@Composable
fun RegisterOptionsScreen(
    onCitizenClick: () -> Unit,
    onDriverClick: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Puedes cambiar tu rol en cualquier momento",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                Text(
                    text = "Elige tu rol",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Selecciona como quieres usar la aplicación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,

                )
            }

            // Tarjeta de Ciudadano
            UserOptionCard(
                title = "Ciudadano",
                description = "Solicita viajes y disfruta de un transporte seguro",
                imageRes = R.drawable.citizen,
                onClick = { onCitizenClick() }
            )

            // Tarjeta de Conductor
            UserOptionCard(
                title = "Conductor",
                description = "Conduce y genera confianza con tu vehículo",
                imageRes = R.drawable.driver,
                onClick = { onDriverClick() }
            )

            Spacer(modifier = Modifier.weight(1f)) // Ocupa el espacio restante
        }
    }
}

@Composable
fun UserOptionCard(
    title: String,
    description: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Se adapta al contenido
            .clickable { onClick() }
            .shadow(elevation = 2.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                colors = CardDefaults.cardColors(
                    containerColor = ITaxCixPaletaColors.Blue1.copy(alpha = 0.1f)
                )
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}