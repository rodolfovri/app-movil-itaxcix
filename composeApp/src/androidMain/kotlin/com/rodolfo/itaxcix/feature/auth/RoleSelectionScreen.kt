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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.rodolfo.itaxcix.domain.model.LoginResult
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay

@Preview
@Composable
fun RoleSelectionScreenPreview() {
    RoleSelectionScreen(
        roles = listOf(
            LoginResult.LoginData.Role(id = 1, name = "Ciudadano"),
            LoginResult.LoginData.Role(id = 2, name = "Conductor")
        ),
        onRoleSelected = {},
        onBackClick = {}
    )
}

@Composable
fun RoleSelectionScreen(
    roles: List<LoginResult.LoginData.Role>,
    onRoleSelected: (LoginResult.LoginData.Role) -> Unit,
    onBackClick: () -> Unit
) {
    var showProgress by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<LoginResult.LoginData.Role?>(null) }

    // Maneja la progresión del estado de carga
    LaunchedEffect(selectedRole) {
        if (selectedRole != null) {
            showProgress = true
            delay(2000) // Simula tiempo de procesamiento
            isSuccess = true
            delay(1500) // Muestra el estado de éxito
            onRoleSelected(selectedRole!!)
        }
    }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selecciona el rol con el que deseas continuar",
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
                    text = "Selecciona tu rol",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Tienes múltiples roles disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
            }

            // Mostrar las opciones de rol disponibles
            roles.forEach { role ->
                when {
                    role.name.contains("CIUDADANO", ignoreCase = true) -> {
                        RoleOptionCard(
                            title = "Ciudadano",
                            description = "Solicita viajes y disfruta de un transporte seguro",
                            imageRes = R.drawable.citizen,
                            onClick = { selectedRole = role } // Ahora pasamos el objeto Role completo
                        )
                    }
                    role.name.contains("CONDUCTOR", ignoreCase = true) -> {
                        RoleOptionCard(
                            title = "Conductor",
                            description = "Conduce y genera confianza con tu vehículo",
                            imageRes = R.drawable.driver,
                            onClick = { selectedRole = role } // Ahora pasamos el objeto Role completo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // Overlay de progreso
        ITaxCixProgressRequest(
            isVisible = showProgress,
            isSuccess = isSuccess,
            loadingTitle = "Configurando rol",
            successTitle = "¡Rol seleccionado!",
            loadingMessage = "Preparando tu experiencia...",
            successMessage = "Redirigiendo al dashboard..."
        )
    }
}

@Composable
fun RoleOptionCard(
    title: String,
    description: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
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