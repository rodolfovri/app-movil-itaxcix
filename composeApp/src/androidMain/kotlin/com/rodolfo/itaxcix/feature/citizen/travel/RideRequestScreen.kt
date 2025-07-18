package com.rodolfo.itaxcix.feature.citizen.travel

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
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
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(driver.location.lat, driver.location.lng), 14f
        )
    }

    val locationPermissionGranted by viewModel.locationPermissionGranted.collectAsState()
    val isLoadingLocation by viewModel.isLoadingLocation.collectAsState()


    // Launcher para solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Permisos concedidos, obtener ubicación
            viewModel.setCurrentLocationAsOrigin()
        } else {
            // Permisos denegados, mostrar mensaje
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Se necesitan permisos de ubicación para usar esta función",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Función para manejar el click de ubicación actual
    val handleCurrentLocationClick = {
        if (viewModel.checkLocationPermission()) {
            // Ya tiene permisos, obtener ubicación directamente
            viewModel.setCurrentLocationAsOrigin()
        } else {
            // Solicitar permisos
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        // Cerrar el bottom sheet
        scope.launch {
            bottomSheetState.hide()
        }.invokeOnCompletion {
            if (!bottomSheetState.isVisible) {
                showBottomSheet = false
            }
        }
    }

    // Agrega este LaunchedEffect en RideRequestScreen para observar cuando termine la carga:
    LaunchedEffect(isLoadingLocation) {
        if (!isLoadingLocation && originLatLng != null && showBottomSheet && isOriginSearch) {
            // Solo cerrar si ya no está cargando Y se obtuvo una ubicación válida
            scope.launch {
                bottomSheetState.hide()
            }.invokeOnCompletion {
                if (!bottomSheetState.isVisible) {
                    showBottomSheet = false
                }
            }
        }
    }

    // También agrega este LaunchedEffect para manejar errores de ubicación:
    LaunchedEffect(locationPermissionGranted) {
        // Si los permisos fueron denegados después de la solicitud, cerrar el bottom sheet
        if (!locationPermissionGranted && !isLoadingLocation && showBottomSheet && isOriginSearch) {
            scope.launch {
                bottomSheetState.hide()
            }.invokeOnCompletion {
                if (!bottomSheetState.isVisible) {
                    showBottomSheet = false
                }
            }
        }
    }

    // Actualizar el ID del conductor cuando se inicia la pantalla
    LaunchedEffect(driver.id) {
        viewModel.updateDriverId(driver.id)
    }

    // Efecto para manejar errores
    LaunchedEffect(key1 = rideRequestState) {
        when (val state = rideRequestState) {
            is RideRequestViewModel.RideRequestState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                Log.e("RideRequestScreen", "Error al solicitar viaje: ${state.message}")
                viewModel.onErrorShown()
            }
            is RideRequestViewModel.RideRequestState.Success -> {
                isSuccessSnackbar = true
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

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = if (isSuccessSnackbar) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                    contentColor = Color.White,
                    dismissAction = {
                        IconButton(onClick = { data.dismiss() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSuccessSnackbar) Icons.Default.Check else Icons.Default.Error,
                            contentDescription = if (isSuccessSnackbar) "Éxito" else "Error",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = data.visuals.message)
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
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
                        .weight(0.5f)
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
                        .weight(0.4f)
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
                        },
                        onCurrentLocationClick = if (isOriginSearch) {
                            {
                                if (viewModel.checkLocationPermission()) {
                                    viewModel.setCurrentLocationAsOrigin()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        } else null,
                        isLoadingLocation = isLoadingLocation
                    )
                }
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
    onPlaceSelected: (AutocompletePrediction) -> Unit,
    onCurrentLocationClick: (() -> Unit)? = null,
    isLoadingLocation: Boolean = false
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
            trailingIcon = {
                if (isOrigin && onCurrentLocationClick != null) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ITaxCixPaletaColors.Blue1,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = onCurrentLocationClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Mi ubicación actual",
                                tint = ITaxCixPaletaColors.Blue1
                            )
                        }
                    }
                }
            },
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 350.dp)
        ) {

            if (isOrigin && onCurrentLocationClick != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isLoadingLocation) {
                                    onCurrentLocationClick()
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 12.dp),
                                color = ITaxCixPaletaColors.Blue1,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Mi ubicación",
                                tint = ITaxCixPaletaColors.Blue1,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isLoadingLocation) "Obteniendo ubicación..." else "Mi ubicación actual",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ITaxCixPaletaColors.Blue1,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isLoadingLocation)
                                    "Por favor espera..."
                                else
                                    "Usar mi ubicación actual como origen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }

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