package com.rodolfo.itaxcix.feature.citizen.travel

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.rodolfo.itaxcix.data.remote.dto.websockets.InitialDriversResponse
import com.rodolfo.itaxcix.feature.citizen.travel.travelViewModel.RideRequestViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixConfirmDialog
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideRequestScreen(
    driver: InitialDriversResponse.DriverInfo,
    onBackClick: () -> Unit = {},
    onNavigateToWaitingScreen: (tripId: Int, driverName: String) -> Unit = {_,_ ->},
    viewModel: RideRequestViewModel = hiltViewModel()
) {

    val rideRequestState by viewModel.rideRequestState.collectAsState()
    val isLoading = rideRequestState is RideRequestViewModel.RideRequestState.Loading
    val isSuccess = rideRequestState is RideRequestViewModel.RideRequestState.Success
    val originQuery by viewModel.originQuery.collectAsState()
    val destinationQuery by viewModel.destinationQuery.collectAsState()
    val originPredictions by viewModel.originPredictions.collectAsState()
    val destinationPredictions by viewModel.destinationPredictions.collectAsState()
    val originLatLng by viewModel.originLatLng.collectAsState()
    val destinationLatLng by viewModel.destinationLatLng.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var isOriginSearch by remember { mutableStateOf(true) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(driver.location.lat, driver.location.lng), 14f
        )
    }

    // Actualizar el ID del conductor cuando se inicia la pantalla
    LaunchedEffect(driver.id) {
        viewModel.updateDriverId(driver.id)
    }

    // Efecto para manejar errores
    LaunchedEffect(key1 = rideRequestState) {
        when (val state = rideRequestState) {
            is RideRequestViewModel.RideRequestState.Error -> {
                val errorMessage = (rideRequestState as RideRequestViewModel.RideRequestState.Error).message
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )

                Log.e("RideRequestScreen", "Error al solicitar viaje: $errorMessage")
                viewModel.onErrorShown()
            }
            is RideRequestViewModel.RideRequestState.Success -> {
                delay(2000) // Mostrar indicador de éxito por 2 segundos
                Log.d("RideRequestScreen", "Viaje solicitado exitosamente")
                viewModel.onSuccessShown()
                onNavigateToWaitingScreen(state.travel.travelId, driver.fullName)
            }
            else -> {}
        }
    }

    LaunchedEffect(routePoints) {
        if (routePoints.isNotEmpty() && originLatLng != null && destinationLatLng != null) {
            val boundsBuilder = LatLngBounds.Builder()
            boundsBuilder.include(originLatLng!!)
            boundsBuilder.include(destinationLatLng!!)

            Log.d("RideRequestScreen", "Puntos de ruta: ${routePoints.size}, Origen: $originLatLng, Destino: $destinationLatLng")

            routePoints.forEach { point ->
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()

            Log.d("RideRequestScreen", "Límites calculados: ${bounds.northeast} - ${bounds.southwest}")


            val padding = 100 // padding en píxeles
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, padding)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Barra superior con el nombre del conductor
            TopAppBar(
                title = { Text(driver.fullName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )

            // Mapa en el medio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
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
                    // Marcador del conductor
                    Marker(
                        state = MarkerState(position = LatLng(driver.location.lat, driver.location.lng)),
                        title = driver.fullName,
                        snippet = "Conductor",
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
                        )
                    )

                    // Marcador de origen
                    originLatLng?.let { origin ->
                        Marker(
                            state = MarkerState(position = origin),
                            title = "Punto de Origen",
                            snippet = originQuery.ifEmpty { "Seleccionar origen" },
                            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN
                            )
                        )
                    }

                    // Marcador de destino
                    destinationLatLng?.let { destination ->
                        Marker(
                            state = MarkerState(position = destination),
                            title = "Punto de Destino",
                            snippet = destinationQuery.ifEmpty { "Seleccionar destino" },
                            icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                            )
                        )
                    }

                    // Dibujar la ruta
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = ITaxCixPaletaColors.Blue1,
                            width = 8f
                        )
                    }
                }
            }

            // Campos de origen y destino
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = ITaxCixPaletaColors.Blue3,
                            shape = RectangleShape
                        )
                        .clickable {
                            isOriginSearch = true
                            showBottomSheet = true
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Origen",
                            fontSize = 12.sp,
                            color = ITaxCixPaletaColors.Blue1
                        )
                        Text(
                            text = if (originQuery.isEmpty()) "Seleccionar origen" else originQuery,
                            fontSize = 16.sp,
                            color = if (originQuery.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = ITaxCixPaletaColors.Blue3,
                            shape = RectangleShape
                        )
                        .clickable {
                            isOriginSearch = false
                            showBottomSheet = true
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Destino",
                            fontSize = 12.sp,
                            color = ITaxCixPaletaColors.Blue1
                        )
                        Text(
                            text = if (destinationQuery.isEmpty()) "Seleccionar destino" else destinationQuery,
                            fontSize = 16.sp,
                            color = if (destinationQuery.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Botón de solicitar viaje
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ITaxCixPaletaColors.Blue1,
                    contentColor = Color.White,
                    disabledContainerColor = ITaxCixPaletaColors.Blue1,
                    disabledContentColor = Color.White
                )
            ) {
                Text("Solicitar Viaje", fontSize = 16.sp)
            }
        }


        // Diálogo de confirmación
        ITaxCixConfirmDialog(
            showDialog = showConfirmDialog,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                viewModel.requestRide()
            },
            title = "Confirmar solicitud",
            message = "¿Estás seguro de solicitar un viaje con ${driver.fullName}?",
            confirmButtonText = "Sí, solicitar",
            dismissButtonText = "Cancelar"
        )

        // Indicador de progreso
        ITaxCixProgressRequest(
            isVisible = isLoading || isSuccess,
            isSuccess = isSuccess,
            loadingTitle = "Procesando tu solicitud",
            successTitle = "¡Viaje solicitado!",
            loadingMessage = "Estamos procesando tu solicitud de viaje...",
            successMessage = "Preparando tu viaje..."
        )

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
                containerColor = Color.White
            ) {
                LocationSearchBottomSheet(
                    isOrigin = isOriginSearch,
                    query = if (isOriginSearch) originQuery else destinationQuery,
                    predictions = if (isOriginSearch) originPredictions else destinationPredictions,
                    onQueryChange = { query ->
                        if (isOriginSearch) viewModel.updateOriginQuery(query)
                        else viewModel.updateDestinationQuery(query)
                    },
                    onPlaceSelected = { prediction ->
                        viewModel.selectPlace(prediction, isOriginSearch)
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }
    }




}

@Composable
fun LocationSearchBottomSheet(
    isOrigin: Boolean,
    query: String,
    predictions: List<AutocompletePrediction>,
    onQueryChange: (String) -> Unit,
    onPlaceSelected: (AutocompletePrediction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = if (isOrigin) "Buscar origen" else "Buscar destino",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(if (isOrigin) "Origen" else "Destino") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ITaxCixPaletaColors.Blue1,
                unfocusedBorderColor = ITaxCixPaletaColors.Blue3,
                cursorColor = ITaxCixPaletaColors.Blue1,
                focusedLabelColor = ITaxCixPaletaColors.Blue1,
                disabledBorderColor = ITaxCixPaletaColors.Blue3,
                disabledLabelColor = ITaxCixPaletaColors.Blue3,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                selectionColors = TextSelectionColors(
                    handleColor = ITaxCixPaletaColors.Blue1,
                    backgroundColor = ITaxCixPaletaColors.Blue3
                )
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 350.dp)
        ) {
            if (predictions.isEmpty() && query.isNotEmpty()) {
                item {
                    Text(
                        "No se encontraron resultados",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = Color.Gray
                    )
                }
            } else {
                items(predictions) { prediction ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaceSelected(prediction) }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = prediction.getPrimaryText(null).toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = prediction.getSecondaryText(null).toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}