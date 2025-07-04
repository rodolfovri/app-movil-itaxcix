package com.rodolfo.itaxcix.feature.citizen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.domain.model.TravelHistoryResult.TravelHistoryData.TravelHistoryItem
import com.rodolfo.itaxcix.feature.citizen.history.citizenHistoryViewModel.CitizenHistoryViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Composable
fun CitizenHistoryScreen(
    viewModel: CitizenHistoryViewModel = hiltViewModel(),
    onNavigateToTravelDetail: (Int, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    LaunchedEffect(key1 = true) {
        viewModel.loadTravelHistory()
    }

    val historyState by viewModel.historyState.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        when (historyState) {
            is CitizenHistoryViewModel.HistoryState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ITaxCixPaletaColors.Blue1)
                }
            }

            is CitizenHistoryViewModel.HistoryState.Error -> {
                val errorState = historyState as CitizenHistoryViewModel.HistoryState.Error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${errorState.message}",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is CitizenHistoryViewModel.HistoryState.Success -> {
                val travelHistory = (historyState as CitizenHistoryViewModel.HistoryState.Success).data.data

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Título y estadísticas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historial de Viajes",
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Total: ${travelHistory.meta.total}",
                            color = ITaxCixPaletaColors.Blue1,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Información de paginación y controles
                    if (travelHistory.meta.total > 0) {
                        PaginationControls(
                            currentPage = currentPage,
                            lastPage = travelHistory.meta.lastPage,
                            total = travelHistory.meta.total,
                            perPage = travelHistory.meta.perPage,
                            onPreviousPage = { viewModel.goToPreviousPage() },
                            onNextPage = { viewModel.goToNextPage() },
                            onGoToPage = { page -> viewModel.goToPage(page) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de viajes
                    if (travelHistory.items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No tienes viajes registrados",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(travelHistory.items) { travel ->
                                TravelHistoryCard(
                                    travel = travel,
                                    onClick = {
                                        onNavigateToTravelDetail(
                                            travel.id,
                                            travel.origin,
                                            travel.destination,
                                            travel.status,
                                            travel.startDate
                                        )
                                    }
                                )
                            }
                        }

                        // Controles de paginación en la parte inferior
                        if (travelHistory.meta.lastPage > 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            PaginationBottomControls(
                                currentPage = currentPage,
                                lastPage = travelHistory.meta.lastPage,
                                onPreviousPage = { viewModel.goToPreviousPage() },
                                onNextPage = { viewModel.goToNextPage() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TravelHistoryCard(
    travel: TravelHistoryItem,
    onClick: () -> Unit = { }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con ID y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Viaje #${travel.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                StatusChip(status = travel.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información del viaje con iconos separados
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Origen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ITaxCixPaletaColors.Blue1,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Origen",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = travel.origin,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Destino
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Destino",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = travel.destination,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            // Fecha si está disponible
            if (travel.startDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fecha: ${travel.startDate}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "SOLICITADO" -> Pair(ITaxCixPaletaColors.Blue1.copy(alpha = 0.1f), ITaxCixPaletaColors.Blue1)
        "ACEPTADO" -> Pair(Color(0xFF2196F3).copy(alpha = 0.1f), Color(0xFF2196F3))
        "INICIADO" -> Pair(Color(0xFFFF9800).copy(alpha = 0.1f), Color(0xFFFF9800))
        "FINALIZADO" -> Pair(Color(0xFF4CAF50).copy(alpha = 0.1f), Color(0xFF4CAF50))
        "CANCELADO" -> Pair(Color(0xFFF44336).copy(alpha = 0.1f), Color(0xFFF44336))
        "RECHAZADO" -> Pair(Color(0xFF9C27B0).copy(alpha = 0.1f), Color(0xFF9C27B0))
        else -> Pair(Color.Gray.copy(alpha = 0.1f), Color.Gray)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    lastPage: Int,
    total: Int,
    perPage: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onGoToPage: (Int) -> Unit
) {
    Column {
        Text(
            text = "Página $currentPage de $lastPage • Mostrando ${minOf(perPage, total)} de $total resultados",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
fun PaginationBottomControls(
    currentPage: Int,
    lastPage: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Anterior
        TextButton(
            onClick = onPreviousPage,
            enabled = currentPage > 1
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Página anterior",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Anterior")
        }

        // Indicador de página actual
        Text(
            text = "$currentPage / $lastPage",
            color = ITaxCixPaletaColors.Blue1,
            fontWeight = FontWeight.Medium
        )

        // Botón Siguiente
        TextButton(
            onClick = onNextPage,
            enabled = currentPage < lastPage
        ) {
            Text("Siguiente")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Página siguiente",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}