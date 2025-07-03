package com.rodolfo.itaxcix.feature.citizen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CardMembership
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.citizen.profile.citizenProfileViewModel.CitizenProfileViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationScreen(
    viewModel: CitizenProfileViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {}
) {
    // Cargar datos al entrar a la pantalla
    LaunchedEffect(key1 = true) {
        viewModel.loadProfileInformation()
    }

    val profileInfoState by viewModel.profileInfoState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información Personal") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ITaxCixPaletaColors.Background)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (profileInfoState) {
                is CitizenProfileViewModel.ProfileInfoState.Loading -> {
                    CircularProgressIndicator(color = ITaxCixPaletaColors.Blue1)
                }

                is CitizenProfileViewModel.ProfileInfoState.Error -> {
                    val errorState = profileInfoState as CitizenProfileViewModel.ProfileInfoState.Error
                    Text(
                        text = "Error: ${errorState.message}",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                is CitizenProfileViewModel.ProfileInfoState.Success -> {
                    val profileInfo = (profileInfoState as CitizenProfileViewModel.ProfileInfoState.Success).profileInfo

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Tarjeta de calificación (mantenerla como está)
                        RatingCard(
                            averageRating = profileInfo.averageRating,
                            ratingsCount = profileInfo.ratingsCount
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Sección de datos personales
                        Text(
                            text = "Datos personales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )

                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "Nombre",
                            value = profileInfo.firstName
                        )

                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "Apellido",
                            value = profileInfo.lastName
                        )

                        InfoRow(
                            icon = Icons.Outlined.Badge,
                            label = "Tipo de documento",
                            value = profileInfo.documentType
                        )

                        InfoRow(
                            icon = Icons.Outlined.CardMembership,
                            label = "Número de documento",
                            value = profileInfo.document
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(24.dp))

                        // Sección de datos de contacto
                        Text(
                            text = "Datos de contacto",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )

                        InfoRow(
                            icon = Icons.Default.Email,
                            label = "Correo electrónico",
                            value = profileInfo.email
                        )

                        InfoRow(
                            icon = Icons.Default.Phone,
                            label = "Teléfono",
                            value = profileInfo.phone
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingCard(averageRating: Double, ratingsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ITaxCixPaletaColors.Blue1),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column {
                Text(
                    text = averageRating.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = "$ratingsCount calificaciones",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ITaxCixPaletaColors.Blue1,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 40.dp),
        color = Color.LightGray.copy(alpha = 0.3f),
        thickness = 0.5.dp
    )
}