package com.rodolfo.itaxcix.feature.driver.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.domain.model.TravelRatingResult
import com.rodolfo.itaxcix.feature.driver.history.driverHistoryViewModel.DriverHistoryViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverTravelDetailScreen(
    travelId: Int,
    origin: String,
    destination: String,
    status: String,
    startDate: String,
    onBackPressed: () -> Unit = {},
    viewModel: DriverHistoryViewModel = hiltViewModel()
) {
    val travelRatingState by viewModel.travelRatingState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(travelId) {
        viewModel.loadTravelRating(travelId)
    }

    LaunchedEffect(travelRatingState) {
        when (val state = travelRatingState) {
            is DriverHistoryViewModel.TravelRatingState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                viewModel.onTravelRatingErrorShown()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del Viaje",
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ITaxCixPaletaColors.Background)
        ) {
            when (travelRatingState) {
                is DriverHistoryViewModel.TravelRatingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ITaxCixPaletaColors.Blue1)
                    }
                }

                is DriverHistoryViewModel.TravelRatingState.Success -> {
                    val ratingData = (travelRatingState as DriverHistoryViewModel.TravelRatingState.Success).data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Información básica del viaje
                        TravelInfoSection(
                            origin = origin,
                            destination = destination,
                            status = status,
                            startDate = startDate
                        )

                        // Sección de calificaciones (solo del conductor al ciudadano)
                        DriverRatingSection(
                            driverRating = ratingData.driverRating
                        )
                    }
                }

                is DriverHistoryViewModel.TravelRatingState.Error -> {
                    ErrorSection()
                }

                else -> {}
            }
        }
    }
}

@Composable
fun TravelInfoSection(
    origin: String,
    destination: String,
    status: String,
    startDate: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Información del Viaje",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ITaxCixPaletaColors.Blue1,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        InfoRow(
            icon = Icons.Default.LocationOn,
            label = "Origen",
            value = origin,
            iconColor = Color(0xFF4CAF50)
        )

        FlexibleDivider()

        InfoRow(
            icon = Icons.Default.LocationOn,
            label = "Destino",
            value = destination,
            iconColor = Color(0xFFF44336)
        )

        FlexibleDivider()

        InfoRow(
            icon = Icons.Default.AccessTime,
            label = "Estado",
            value = status,
            iconColor = getStatusColor(status)
        )

        if (startDate.isNotEmpty()) {
            FlexibleDivider()
            InfoRow(
                icon = Icons.Default.DateRange,
                label = "Fecha",
                value = startDate,
                iconColor = ITaxCixPaletaColors.Blue1
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color = ITaxCixPaletaColors.Blue1
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FlexibleDivider() {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(
        color = Color.Gray.copy(alpha = 0.2f),
        thickness = 1.dp
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun DriverRatingSection(
    driverRating: TravelRatingResult.Rating?
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Tu Calificación",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ITaxCixPaletaColors.Blue1,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Calificación que le diste al pasajero",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (driverRating) {
            null -> {
                NoRatingSection()
            }
            else -> {
                DriverRatingDetails(rating = driverRating)
            }
        }
    }
}

@Composable
fun DriverRatingDetails(rating: TravelRatingResult.Rating) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Información de quién calificó a quién
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Calificaste a ${rating.ratedName}",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        // Estrellas y puntuación
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < rating.score) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.size(28.dp)
                )
                if (index < 4) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${rating.score}/5",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ITaxCixPaletaColors.Blue1
            )
        }

        // Comentario si existe
        if (rating.comment.isNotEmpty()) {
            Text(
                text = "Tu comentario:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = rating.comment,
                fontSize = 16.sp,
                color = Color.Black,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fecha si está disponible
        if (rating.createdAt.isNotEmpty()) {
            Text(
                text = "Fecha de calificación: ${rating.createdAt}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun NoRatingSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.4f),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sin calificación",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No has calificado este viaje",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error al cargar el detalle",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No se pudo cargar la información del viaje",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        "SOLICITADO" -> Color(0xFF2196F3)      // Azul - Solicitud pendiente
        "ACEPTADO" -> Color(0xFF4CAF50)        // Verde - Aceptado por el conductor
        "INICIADO" -> Color(0xFFFF9800)        // Naranja - Viaje en progreso
        "FINALIZADO" -> Color(0xFF8BC34A)      // Verde claro - Viaje completado
        "CANCELADO" -> Color(0xFFF44336)       // Rojo - Cancelado
        "RECHAZADO" -> Color(0xFF9C27B0)       // Púrpura - Rechazado por el conductor
        else -> Color.Gray
    }
}