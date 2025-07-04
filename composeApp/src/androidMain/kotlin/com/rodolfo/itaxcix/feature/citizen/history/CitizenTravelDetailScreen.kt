package com.rodolfo.itaxcix.feature.citizen.history

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
import androidx.compose.material3.MaterialTheme
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
import com.rodolfo.itaxcix.feature.citizen.history.citizenHistoryViewModel.CitizenHistoryViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenTravelDetailScreen(
    travelId: Int,
    origin: String,
    destination: String,
    status: String,
    startDate: String,
    onBackPressed: () -> Unit = {},
    viewModel: CitizenHistoryViewModel = hiltViewModel()
) {
    val travelRatingState by viewModel.travelRatingState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(travelId) {
        viewModel.loadTravelRating(travelId)
    }

    LaunchedEffect(travelRatingState) {
        when (val state = travelRatingState) {
            is CitizenHistoryViewModel.TravelRatingState.Error -> {
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
            when (travelRatingState) {
                is CitizenHistoryViewModel.TravelRatingState.Loading -> {
                    CircularProgressIndicator(color = ITaxCixPaletaColors.Blue1)
                }

                is CitizenHistoryViewModel.TravelRatingState.Success -> {
                    val ratingData = (travelRatingState as CitizenHistoryViewModel.TravelRatingState.Success).data

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Sección de información del viaje
                        Text(
                            text = "Información del viaje",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )

                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Origen",
                            value = origin,
                            iconColor = Color(0xFF4CAF50)
                        )

                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            label = "Destino",
                            value = destination,
                            iconColor = Color(0xFFF44336)
                        )

                        InfoRow(
                            icon = Icons.Default.AccessTime,
                            label = "Estado",
                            value = status,
                            iconColor = getStatusColor(status)
                        )

                        if (startDate.isNotEmpty()) {
                            InfoRow(
                                icon = Icons.Default.DateRange,
                                label = "Fecha",
                                value = startDate,
                                iconColor = ITaxCixPaletaColors.Blue1
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(24.dp))

                        // Sección de calificación
                        Text(
                            text = "Tu calificación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )

                        when (ratingData.citizenRating) {
                            null -> {
                                NoRatingSection()
                            }
                            else -> {
                                CitizenRatingDetails(rating = ratingData.citizenRating!!)
                            }
                        }
                    }
                }

                is CitizenHistoryViewModel.TravelRatingState.Error -> {
                    ErrorSection()
                }

                else -> {}
            }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
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

@Composable
fun CitizenRatingDetails(rating: TravelRatingResult.Rating) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Información de quién calificó a quién
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = ITaxCixPaletaColors.Blue1,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Conductor calificado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = rating.ratedName,
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

        // Calificación con estrellas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Calificación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < rating.score) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                        if (index < 4) {
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${rating.score}/5",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (rating.comment.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 40.dp),
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            // Comentario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = "Tu comentario",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = rating.comment,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (rating.createdAt.isNotEmpty()) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 40.dp),
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )

            // Fecha de calificación
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = "Fecha de calificación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = rating.createdAt,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sin calificación",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No has calificado este viaje",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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