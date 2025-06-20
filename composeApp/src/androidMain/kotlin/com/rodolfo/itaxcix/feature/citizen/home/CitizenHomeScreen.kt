package com.rodolfo.itaxcix.feature.citizen.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapUiSettings
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.data.remote.dto.websockets.InitialDriversResponse
import com.rodolfo.itaxcix.feature.citizen.viewModelCitizen.CitizenHomeViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.utils.ImageUtils

@Preview
@Composable
fun CitizenHomeScreenPreview() {
    CitizenHomeScreen()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CitizenHomeScreen(
    viewModel: CitizenHomeViewModel = hiltViewModel(),
    onNavigateToRideRequest: (InitialDriversResponse.DriverInfo) -> Unit = {}
) {
    val drivers by viewModel.availableDrivers.collectAsState()
    val isLoading by viewModel.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Mapa con conductores (ocupa 60% de la pantalla)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
        ) {
            GoogleMapView(drivers = drivers)

            // Indicador de carga sobre el mapa
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        // Lista de conductores (ocupa 40% de la pantalla)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(Color.White)
        ) {
            Text(
                text = "Conductores disponibles (${drivers.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp)
            )

            if (drivers.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No hay conductores disponibles", color = Color.Gray)
                }
            } else {
                // Lista con animaciones
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(
                        items = drivers,
                        key = { driver -> driver.id } // Usar ID como clave estable
                    ) { driver ->
                        Modifier.fillMaxWidth()
                        DriverCard(
                            driver = driver,
                            modifier = Modifier.animateItem(
                                    fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ).padding(vertical = 4.dp),
                            onRequestRideClick = { selectedDriver ->
                                onNavigateToRideRequest(selectedDriver)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleMapView(drivers: List<InitialDriversResponse.DriverInfo>) {
    val singaporeLatLng = remember { LatLng(1.35, 103.87) } // Centro predeterminado
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singaporeLatLng, 12f)
    }

    // Usar la ubicación de un conductor como centro si hay conductores
    LaunchedEffect(drivers) {
        if (drivers.isNotEmpty()) {
            val firstDriver = drivers.first()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(firstDriver.location.lat, firstDriver.location.lng),
                14f
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL,
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true
        )
    ) {
        // Añadir marcadores para cada conductor
        drivers.forEach { driver ->
            val position = LatLng(driver.location.lat, driver.location.lng)
            Marker(
                state = MarkerState(position = position),
                title = driver.fullName,
                snippet = "★ ${String.format("%.1f", driver.rating)}"
            )
        }
    }
}

@Composable
fun DriverCard(
    driver: InitialDriversResponse.DriverInfo,
    modifier: Modifier = Modifier,
    onRequestRideClick: (InitialDriversResponse.DriverInfo) -> Unit = {} // Añadir parámetro para manejar clic
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3F51B5)),
                contentAlignment = Alignment.Center
            ) {
                if (driver.image.isNotEmpty()) {
                    val bitmap = ImageUtils.decodeBase64ToBitmap(driver.image)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_image),
                            contentDescription = "Foto de perfil predeterminada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile_image),
                        contentDescription = "Foto de perfil predeterminada",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Información del conductor
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = driver.fullName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Calificación",
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = String.format("%.1f", driver.rating),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            IconButton(
                onClick = { onRequestRideClick(driver) },
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = "Solicitar viaje",
                    tint = ITaxCixPaletaColors.Blue1,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
