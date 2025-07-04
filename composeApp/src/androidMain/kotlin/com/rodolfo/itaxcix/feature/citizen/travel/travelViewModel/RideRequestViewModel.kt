package com.rodolfo.itaxcix.feature.citizen.travel.travelViewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.rodolfo.itaxcix.data.local.PreferencesManager
import com.rodolfo.itaxcix.data.remote.dto.travel.TravelRequestDTO
import com.rodolfo.itaxcix.data.remote.dto.common.DirectionsResponse
import com.rodolfo.itaxcix.domain.model.TravelResult
import com.rodolfo.itaxcix.domain.repository.CitizenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class RideRequestViewModel @Inject constructor(
    private val placesClient: PlacesClient,
    private val citizenRepository: CitizenRepository,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _originQuery = MutableStateFlow("")
    private val _destinationQuery = MutableStateFlow("")
    private val _originPredictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    private val _destinationPredictions = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    private val _selectedOrigin = MutableStateFlow<AutocompletePrediction?>(null)
    private val _selectedDestination = MutableStateFlow<AutocompletePrediction?>(null)
    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    private val _originLatLng = MutableStateFlow<LatLng?>(null)
    private val _destinationLatLng = MutableStateFlow<LatLng?>(null)

    val originQuery: StateFlow<String> = _originQuery
    val destinationQuery: StateFlow<String> = _destinationQuery
    val originPredictions: StateFlow<List<AutocompletePrediction>> = _originPredictions
    val destinationPredictions: StateFlow<List<AutocompletePrediction>> = _destinationPredictions
    val selectedOrigin: StateFlow<AutocompletePrediction?> = _selectedOrigin
    val selectedDestination: StateFlow<AutocompletePrediction?> = _selectedDestination
    val routePoints: StateFlow<List<LatLng>> = _routePoints
    val originLatLng: StateFlow<LatLng?> = _originLatLng
    val destinationLatLng: StateFlow<LatLng?> = _destinationLatLng

    private val sessionToken = AutocompleteSessionToken.newInstance()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation

    // Verificar permisos de ubicación
    fun checkLocationPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _locationPermissionGranted.value = hasPermission
        return hasPermission
    }

    // Método para buscar lugares
    fun updateOriginQuery(query: String) {
        _originQuery.value = query
        if (query.length > 1) {
            searchPlaces(query, true)
        } else {
            _originPredictions.value = emptyList()
        }
    }

    // Método para buscar lugares
    fun updateDestinationQuery(query: String) {
        _destinationQuery.value = query
        if (query.length > 1) {
            searchPlaces(query, false)
        } else {
            _destinationPredictions.value = emptyList()
        }
    }

    fun setCurrentLocationAsOrigin() {
        viewModelScope.launch {
            try {
                _isLoadingLocation.value = true

                if (!checkLocationPermission()) {
                    Log.e("RideRequestViewModel", "Permisos de ubicación no concedidos")
                    _isLoadingLocation.value = false
                    return@launch
                }

                val location = getCurrentLocation()

                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    val address = getAddressFromLocation(location.latitude, location.longitude)

                    // Combinar dirección y distrito para mostrar en el campo
                    val displayText = "${address.streetAddress}, ${address.district.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() } }}"

                    _originLatLng.value = currentLatLng
                    _originLatitude.value = location.latitude
                    _originLongitude.value = location.longitude
                    _originQuery.value = displayText
                    _originAddress.value = address.streetAddress
                    _originDistrict.value = address.district
                    _originPredictions.value = emptyList()

                    Log.d("RideRequestViewModel", "Ubicación actual: $displayText")

                    if (_destinationLatLng.value != null) {
                        calculateRoute()
                    }
                } else {
                    Log.e("RideRequestViewModel", "No se pudo obtener la ubicación actual")
                }
            } catch (e: Exception) {
                Log.e("RideRequestViewModel", "Error obteniendo ubicación actual: ${e.message}")
            } finally {
                _isLoadingLocation.value = false
            }
        }
    }

    data class AddressInfo(
        val streetAddress: String,
        val district: String
    )

    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): AddressInfo {
        return try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())

            // Usar geocodificación inversa para obtener la dirección
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // Para API 33+
                suspendCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        if (address != null) {
                            val streetAddress = buildString {
                                address.thoroughfare?.let { append("$it ") }
                                address.subThoroughfare?.let { append(it) }
                            }.trim().ifEmpty { "Dirección no disponible" }

                            // CAMBIO PRINCIPAL: Priorizar locality sobre subLocality para obtener el distrito
                            val district = address.locality?.uppercase()
                                ?: address.subAdminArea?.uppercase() // Nivel administrativo sub (distrito)
                                ?: address.adminArea?.uppercase()    // Nivel administrativo principal (provincia/región)
                                ?: "DISTRITO_NO_DISPONIBLE"

                            // Log para debugging
                            Log.d("GeoCoding", "subLocality: ${address.subLocality}")
                            Log.d("GeoCoding", "locality: ${address.locality}")
                            Log.d("GeoCoding", "subAdminArea: ${address.subAdminArea}")
                            Log.d("GeoCoding", "adminArea: ${address.adminArea}")
                            Log.d("GeoCoding", "Distrito seleccionado: $district")

                            continuation.resume(AddressInfo(streetAddress, district))
                        } else {
                            continuation.resume(AddressInfo("Ubicación actual", "UBICACION_ACTUAL"))
                        }
                    }
                }
            } else {
                // Para versiones anteriores
                withContext(Dispatchers.IO) {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()

                    if (address != null) {
                        val streetAddress = buildString {
                            address.thoroughfare?.let { append("$it ") }
                            address.subThoroughfare?.let { append(it) }
                        }.trim().ifEmpty { "Dirección no disponible" }

                        // CAMBIO PRINCIPAL: Priorizar locality sobre subLocality para obtener el distrito
                        val district = address.locality?.uppercase()
                            ?: address.subAdminArea?.uppercase() // Nivel administrativo sub (distrito)
                            ?: address.adminArea?.uppercase()    // Nivel administrativo principal (provincia/región)
                            ?: "DISTRITO_NO_DISPONIBLE"

                        // Log para debugging
                        Log.d("GeoCoding", "subLocality: ${address.subLocality}")
                        Log.d("GeoCoding", "locality: ${address.locality}")
                        Log.d("GeoCoding", "subAdminArea: ${address.subAdminArea}")
                        Log.d("GeoCoding", "adminArea: ${address.adminArea}")
                        Log.d("GeoCoding", "Distrito seleccionado: $district")

                        AddressInfo(streetAddress, district)
                    } else {
                        AddressInfo("Ubicación actual", "UBICACION_ACTUAL")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RideRequestViewModel", "Error en geocodificación inversa: ${e.message}")
            AddressInfo("Ubicación actual", "UBICACION_ACTUAL")
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        return try {
            if (!checkLocationPermission()) {
                return null
            }

            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
        } catch (e: Exception) {
            Log.e("RideRequestViewModel", "Error getting current location: ${e.message}")
            null
        }
    }

    // Método para buscar lugares con el Places API
    private fun searchPlaces(query: String, isOrigin: Boolean) {

        // Define las coordenadas del rectángulo que abarca la región de Lambayeque
        val southwestLat = -6.9500  // Latitud suroeste de la provincia de Chiclayo
        val southwestLng = -79.9500 // Longitud suroeste de la provincia de Chiclayo
        val northeastLat = -6.6500  // Latitud noreste de la provincia de Chiclayo
        val northeastLng = -79.6500 // Longitud noreste de la provincia de Chiclayo

        // Crear el rectángulo delimitador
        val bounds = RectangularBounds.newInstance(
            LatLng(southwestLat, southwestLng),  // Esquina suroeste
            LatLng(northeastLat, northeastLng)   // Esquina noreste
        )


        val request = FindAutocompletePredictionsRequest.builder()
            .setTypesFilter(listOf(PlaceTypes.ADDRESS))
            .setSessionToken(sessionToken)
            .setCountries("PE") // Establecer el país a Perú
            //.setLocationRestriction(bounds) // Restringir a la región de Lambayeque
            .setQuery(query)
            .build()

        viewModelScope.launch {
            try {
                placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                    if (isOrigin) {
                        _originPredictions.value = response.autocompletePredictions
                    } else {
                        _destinationPredictions.value = response.autocompletePredictions
                    }
                }
            } catch (e: Exception) {
                Log.e("RideRequestViewModel", "Error fetching predictions: ${e.message}")
            }
        }
    }

    // Método para seleccionar un lugar
    fun selectPlace(prediction: AutocompletePrediction, isOrigin: Boolean) {
        if (isOrigin) {
            getPlaceLocation(prediction.placeId) { location ->
                _selectedOrigin.value = prediction
                _originLatLng.value = location
                _originLatitude.value = location.latitude
                _originLongitude.value = location.longitude

                // Extraer calle y distrito
                val streetName = prediction.getPrimaryText(null).toString()
                val fullSecondaryText = prediction.getSecondaryText(null).toString()
                val districtOnly = fullSecondaryText.split(",")[0].trim()

                // Combinar calle y distrito para mostrar en el campo
                val displayText = "$streetName, $districtOnly"

                _originQuery.value = displayText
                _originAddress.value = streetName
                _originDistrict.value = districtOnly.uppercase()
                _originPredictions.value = emptyList()

                Log.d("Origen", "Calle: $streetName, Distrito: $districtOnly, Display: $displayText")

                if (_destinationLatLng.value != null) {
                    calculateRoute()
                }
            }
        } else {
            getPlaceLocation(prediction.placeId) { location ->
                _selectedDestination.value = prediction
                _destinationLatLng.value = location
                _destinationLatitude.value = location.latitude
                _destinationLongitude.value = location.longitude

                // Extraer calle y distrito
                val streetName = prediction.getPrimaryText(null).toString()
                val fullSecondaryText = prediction.getSecondaryText(null).toString()
                val districtOnly = fullSecondaryText.split(",")[0].trim()

                // Combinar calle y distrito para mostrar en el campo
                val displayText = "$streetName, $districtOnly"

                _destinationQuery.value = displayText
                _destinationAddress.value = streetName
                _destinationDistrict.value = districtOnly.uppercase()
                _destinationPredictions.value = emptyList()

                Log.d("Destino", "Calle: $streetName, Distrito: $districtOnly, Display: $displayText")

                if (_originLatLng.value != null) {
                    calculateRoute()
                }
            }
        }
    }

    // Método para obtener la ubicación del lugar seleccionado
    private fun getPlaceLocation(placeId: String, callback: (LatLng) -> Unit) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        try {
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let { callback(it) }
            }.addOnFailureListener { exception ->
                Log.e("RideRequestViewModel", "Error fetching place: ${exception.message}")
            }
        } catch (e: Exception) {
            Log.e("RideRequestViewModel", "Error in getPlaceLocation: ${e.message}")
        }
    }

    private fun calculateRoute() {
        val origin = _originLatLng.value
        val destination = _destinationLatLng.value

        if (origin != null && destination != null) {
            viewModelScope.launch {
                try {
                    // Construir la URL para la API de Directions
                    val apiKey = "AIzaSyBFVuOButa5EMduTqE4_iis8T6yKyhdpvI"
                    val baseUrl = "https://maps.googleapis.com/maps/api/directions/json"
                    val originParam = "${origin.latitude},${origin.longitude}"
                    val destParam = "${destination.latitude},${destination.longitude}"
                    val url = "$baseUrl?origin=$originParam&destination=$destParam&key=$apiKey"

                    Log.d("RideRequestViewModel", "URL de la solicitud: $url")

                    // Crear un cliente HTTP para hacer la petición
                    val client = HttpClient {
                        install(ContentNegotiation) {
                            json(Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                prettyPrint = true
                            })
                        }
                    }

                    // Hacer la petición a la API
                    val response: DirectionsResponse = client.get(url).body()

                    Log.d("RideRequestViewModel", "Respuesta de la API: $response")

                    // Procesar la respuesta si existe una ruta
                    if (response.routes.isNotEmpty()) {
                        val route = response.routes[0]
                        val points = mutableListOf<LatLng>()

                        // Decodificar la polyline de la ruta completa
                        val polylinePoints = decodePoly(route.overview_polyline.points)
                        points.addAll(polylinePoints)

                        // Actualizar el estado con los puntos obtenidos
                        _routePoints.value = points
                    } else {
                        Log.e("RideRequestViewModel", "No se encontraron rutas")
                    }

                    client.close()

                } catch (e: Exception) {
                    Log.e("RideRequestViewModel", "Error calculando ruta: ${e.message}")
                }
            }
        } else {
            Log.d("RideRequestViewModel", "Origen o destino no disponible")
        }
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        Log.d("RideRequestViewModel", "Decodificando polyline: ${encoded.take(20)}...")

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)

            if (poly.isNotEmpty()) {
                Log.d("RideRequestViewModel", "Polyline decodificada: ${poly.size} puntos. Primer punto: ${poly.first()}, Último punto: ${poly.last()}")
            } else {
                Log.e("RideRequestViewModel", "No se generaron puntos al decodificar la polyline")
            }
        }
        return poly
    }

    private val _rideRequestState = MutableStateFlow<RideRequestState>(RideRequestState.Initial)
    val rideRequestState: StateFlow<RideRequestState> = _rideRequestState.asStateFlow()

    private val _citizenId = MutableStateFlow<Int?>(null)
    private val _driverId = MutableStateFlow<Int?>(null)
    private val _originLatitude = MutableStateFlow<Double?>(null)
    private val _originLongitude = MutableStateFlow<Double?>(null)
    private val _originDistrict = MutableStateFlow<String?>(null)
    private val _originAddress = MutableStateFlow<String?>(null)
    private val _destinationLatitude = MutableStateFlow<Double?>(null)
    private val _destinationLongitude = MutableStateFlow<Double?>(null)
    private val _destinationDistrict = MutableStateFlow<String?>(null)
    private val _destinationAddress = MutableStateFlow<String?>(null)

    val citizenId: StateFlow<Int?> = _citizenId
    val driverId: StateFlow<Int?> = _driverId
    val originLatitude: StateFlow<Double?> = _originLatitude
    val originLongitude: StateFlow<Double?> = _originLongitude
    val originDistrict: StateFlow<String?> = _originDistrict
    val originAddress: StateFlow<String?> = _originAddress
    val destinationLatitude: StateFlow<Double?> = _destinationLatitude
    val destinationLongitude: StateFlow<Double?> = _destinationLongitude
    val destinationDistrict: StateFlow<String?> = _destinationDistrict
    val destinationAddress: StateFlow<String?> = _destinationAddress

    private val _originError = MutableStateFlow<String?>(null)
    private val _destinationError = MutableStateFlow<String?>(null)
    private val _generalError = MutableStateFlow<String?>(null)

    val originError: StateFlow<String?> = _originError.asStateFlow()
    val destinationError: StateFlow<String?> = _destinationError.asStateFlow()
    val generalError: StateFlow<String?> = _generalError.asStateFlow()

    fun updateCitizenId(id: Int) {
        _citizenId.value = id
    }

    fun updateDriverId(id: Int) {
        _driverId.value = id
    }

    fun updateOriginCoordinates(latitude: Double, longitude: Double) {
        _originLatitude.value = latitude
        _originLongitude.value = longitude
    }

    fun updateOriginDetails(district: String, address: String) {
        _originDistrict.value = district
        _originAddress.value = address
    }

    fun updateDestinationCoordinates(latitude: Double, longitude: Double) {
        _destinationLatitude.value = latitude
        _destinationLongitude.value = longitude
    }

    fun updateDestinationDetails(district: String, address: String) {
        _destinationDistrict.value = district
        _destinationAddress.value = address
    }

    // Inicializar el ID del ciudadano desde las preferencias
    init {
        viewModelScope.launch {
            preferencesManager.userData.collect { userData ->
                userData?.let {
                    _citizenId.value = it.id
                }
            }
        }
    }

    private fun validateOrigin(): Boolean {
        if (_originLatitude.value == null || _originLongitude.value == null) {
            _originError.value = "Por favor, selecciona un origen válido."
            return false
        }

        if (_originDistrict.value.isNullOrEmpty() || _originAddress.value.isNullOrEmpty()) {
            _originError.value = "Por favor, completa los detalles del origen."
            return false
        }

        _originError.value = null // Limpiar error si es válido
        return true
    }

    private fun validateDestination(): Boolean {
        if (_destinationLatitude.value == null || _destinationLongitude.value == null) {
            _destinationError.value = "Por favor, selecciona un destino válido."
            return false
        }

        if (_destinationDistrict.value.isNullOrEmpty() || _destinationAddress.value.isNullOrEmpty()) {
            _destinationError.value = "Por favor, completa los detalles del destino."
            return false
        }

        _destinationError.value = null // Limpiar error si es válido
        return true
    }

    // Agregar esta función de validación al RideRequestViewModel
    private fun validateDifferentLocations(): Boolean {
        val origin = _originLatLng.value
        val destination = _destinationLatLng.value

        if (origin != null && destination != null) {
            // Calcular distancia entre puntos (en metros)
            val distance = calculateDistance(origin, destination)

            if (distance < 100) { // Si están muy cerca (menos de 100 metros)
                _generalError.value = "El origen y destino deben ser diferentes"
                return false
            }
        }

        _generalError.value = null
        return true
    }

    private fun calculateDistance(origin: LatLng, destination: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            origin.latitude, origin.longitude,
            destination.latitude, destination.longitude,
            results
        )
        return results[0]
    }

    private fun resetStateIfError() {
        if (_rideRequestState.value is RideRequestState.Error) {
            _rideRequestState.value = RideRequestState.Initial
        }
    }

    private fun validateFields(): Pair<Boolean, String?> {
        val errorMessages = mutableListOf<String>()
        var isValid = true

        if (_citizenId.value == null) {
            errorMessages.add("El ID del ciudadano es obligatorio.")
            isValid = false
        }

        if (_driverId.value == null) {
            errorMessages.add("El ID del conductor es obligatorio.")
            isValid = false
        }

        if (_originLatitude.value == null || _originLongitude.value == null) {
            _originError.value = "Las coordenadas de origen son obligatorias."
            errorMessages.add("Las coordenadas de origen son obligatorias.")
            isValid = false
        } else if (_originAddress.value.isNullOrEmpty()) {
            _originError.value = "La dirección de origen es obligatoria."
            errorMessages.add("La dirección de origen es obligatoria.")
            isValid = false
        }

        if (_destinationLatitude.value == null || _destinationLongitude.value == null) {
            _destinationError.value = "Las coordenadas de destino son obligatorias."
            errorMessages.add("Las coordenadas de destino son obligatorias.")
            isValid = false
        } else if (_destinationAddress.value.isNullOrEmpty()) {
            _destinationError.value = "La dirección de destino es obligatoria."
            errorMessages.add("La dirección de destino es obligatoria.")
            isValid = false
        }

        // Validar que origen y destino sean diferentes
        if (!validateDifferentLocations()) {
            errorMessages.add("El origen y destino deben ser diferentes.")
            isValid = false
        }

        return Pair(isValid, if (errorMessages.isNotEmpty()) errorMessages.joinToString("\n") else null)
    }

    fun requestRide() {
        _rideRequestState.value = RideRequestState.Initial

        val (isValid, errorMessage) = validateFields()
        if (!isValid) {
            _rideRequestState.value = RideRequestState.Error(errorMessage ?: "Error desconocido")
            return
        }

        viewModelScope.launch {
            try {
                _rideRequestState.value = RideRequestState.Loading

                val request = TravelRequestDTO(
                    citizenId = _citizenId.value ?: 0,
                    driverId = _driverId.value ?: 0,
                    originLatitude = _originLatitude.value ?: 0.0,
                    originLongitude = _originLongitude.value ?: 0.0,
                    originDistrict = _originDistrict.value ?: "",
                    originAddress = _originAddress.value ?: "",
                    destinationLatitude = _destinationLatitude.value ?: 0.0,
                    destinationLongitude = _destinationLongitude.value ?: 0.0,
                    destinationDistrict = _destinationDistrict.value ?: "",
                    destinationAddress = _destinationAddress.value ?: ""
                )

                Log.d("RideRequestViewModel", "Solicitando viaje con datos: $request")

                val result = citizenRepository.travels(request)

                Log.d("RideRequestViewModel", "Resultado de la solicitud de viaje: $result")

                _rideRequestState.value = RideRequestState.Success(result)
            } catch (e: Exception) {
                Log.e("RideRequestViewModel", "Error al solicitar el viaje: ${e.message}")
                _rideRequestState.value = RideRequestState.Error(e.message ?: "Error desconocido al solicitar el viaje")
            }
        }
    }

    fun onErrorShown() {
        if (_rideRequestState.value is RideRequestState.Error) {
            _rideRequestState.value = RideRequestState.Initial
        }
    }

    fun onSuccessShown() {
        if (_rideRequestState.value is RideRequestState.Success) {
            _rideRequestState.value = RideRequestState.Initial
        }
    }

    sealed class RideRequestState {
        object Initial : RideRequestState()
        object Loading : RideRequestState()
        data class Success(val travel: TravelResult) : RideRequestState()
        data class Error(val message: String) : RideRequestState()
    }

}