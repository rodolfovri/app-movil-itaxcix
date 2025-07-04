package com.rodolfo.itaxcix.feature.driver.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.TaxiAlert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.data.local.UserData
import com.rodolfo.itaxcix.feature.driver.profile.driverProfileViewModel.DriverProfileViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import com.rodolfo.itaxcix.utils.ImageUtils
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Preview
@Composable
fun DriverProfileScreenPreview() {
    DriverProfileScreen()
}

@Composable
fun DriverProfileScreen(
    viewModel: DriverProfileViewModel = hiltViewModel(),
    onNavigateToPersonalInfo: () -> Unit = { },
    onNavigateToChangeEmail: () -> Unit = { },
    onNavigateToChangePhone: () -> Unit = { },
    onNavigateToVehicleAssociation: () -> Unit = { },
) {
    val userData by viewModel.userData.collectAsState()
    val driverToCitizenState by viewModel.driverToCitizenState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val isLoadingProfileImage by viewModel.isLoadingProfileImage.collectAsState()
    val vehicleDisassociationState by viewModel.vehicleDisassociationState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isLoading = uploadState is DriverProfileViewModel.UploadState.Loading
    val isSuccess = uploadState is DriverProfileViewModel.UploadState.Success

    val isDTCLoading = driverToCitizenState is DriverProfileViewModel.DriverToCitizenState.Loading
    val isDTCSuccess = driverToCitizenState is DriverProfileViewModel.DriverToCitizenState.Success

    val isVDLoading = vehicleDisassociationState is DriverProfileViewModel.VehicleDisassociationState.Loading
    val isVDSuccess = vehicleDisassociationState is DriverProfileViewModel.VehicleDisassociationState.Success

    // Estados para el Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    // Efecto para mostrar Snackbar cuando hay error en driver to citizen
    LaunchedEffect(key1 = driverToCitizenState) {
        when (val state = driverToCitizenState) {
            is DriverProfileViewModel.DriverToCitizenState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    // Agregar este LaunchedEffect para vehicle disassociation
    LaunchedEffect(key1 = vehicleDisassociationState) {
        when (val state = vehicleDisassociationState) {
            is DriverProfileViewModel.VehicleDisassociationState.Error -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    // Lanzador para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val tempFile = ImageUtils.createTempFileFromUri(context, uri)

                    viewModel.uploadProfilePhoto(tempFile)
                } catch (e: Exception) {
                    Log.e("DriverProfile", "Error al procesar imagen: ${e.message}")
                }
            }
        }
    }



    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
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
            containerColor = ITaxCixPaletaColors.Background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                ProfileHeader(
                    userData = userData,
                    isLoadingProfileImage = isLoadingProfileImage,
                    onImageClick = { galleryLauncher.launch("image/*") }
                )
                Spacer(modifier = Modifier.height(24.dp))
                MenuButtons(
                    onNavigateToPersonalInfo = onNavigateToPersonalInfo,
                    onNavigateToChangeEmail = onNavigateToChangeEmail,
                    onNavigateToChangePhone = onNavigateToChangePhone,
                    onConvertToCitizen = { viewModel.convertToCitizen() },
                    onDisassociateVehicle = { viewModel.disassociateVehicle() },
                    onNavigateToVehicleAssociation = onNavigateToVehicleAssociation
                )
            }
        }

        // Progress para la carga de imagen de perfil
        ITaxCixProgressRequest(
            isVisible = isLoading || isSuccess,
            isSuccess = isSuccess,
            loadingTitle = "Subiendo foto",
            successTitle = "¡Foto actualizada!",
            loadingMessage = "Estamos procesando tu nueva foto de perfil...",
            successMessage = "Tu foto de perfil ha sido actualizada correctamente"
        )

        // Progress para driver to citizen
        ITaxCixProgressRequest(
            isVisible = isDTCLoading || isDTCSuccess,
            isSuccess = isDTCSuccess,
            loadingTitle = "Procesando solicitud",
            successTitle = "¡Solicitud enviada!",
            loadingMessage = "Estamos procesando tu solicitud para convertirte en ciudadano...",
            successMessage = "Tu solicitud ha sido enviada correctamente. Te notificaremos cuando sea aprobada."
        )

        // Agregar este ITaxCixProgressRequest para vehicle disassociation
        ITaxCixProgressRequest(
            isVisible = isVDLoading || isVDSuccess,
            isSuccess = isVDSuccess,
            loadingTitle = "Desasociando vehículo",
            successTitle = "¡Vehículo desasociado!",
            loadingMessage = "Estamos procesando la desasociación de tu vehículo...",
            successMessage = "Tu vehículo ha sido desasociado correctamente."
        )
    }
}

@Composable
private fun ProfileHeader(
    userData: UserData?,
    isLoadingProfileImage: Boolean = false,
    onImageClick: () -> Unit = { }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, ITaxCixPaletaColors.Blue1, CircleShape)
                    .clickable { onImageClick() }
            ) {
                when {
                    isLoadingProfileImage -> {
                        CircularProgressIndicator(
                            color = ITaxCixPaletaColors.Blue1,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                    // Mostrar imagen decodificada o imagen por defecto
                    else -> {
                        ProfileImage(userData?.profileImage, onImageClick)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ITaxCixPaletaColors.Blue1)
                    .clickable { onImageClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Cambiar foto",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = userData?.fullName ?: "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userData?.roles?.firstOrNull() ?: "Conductor",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .background(
                    color = ITaxCixPaletaColors.Blue1,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        RatingBar(
            rating = userData?.rating?.toFloat() ?: 0f,
            modifier = Modifier.height(24.dp)
        )
    }
}

// Componente para mostrar la imagen de perfil
@Composable
private fun ProfileImage(
    base64Image: String?,
    onImageClick: () -> Unit
) {
    if (!base64Image.isNullOrEmpty()) {
        val bitmap = ImageUtils.decodeBase64ToBitmap(base64Image)

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            DefaultProfileImage(onImageClick)
        }
    } else {
        DefaultProfileImage(onImageClick)
    }
}

@Composable
private fun DefaultProfileImage(onImageClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onImageClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.default_profile_image),
            contentDescription = "Foto de perfil predeterminada",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MenuButtons(
    onNavigateToPersonalInfo: () -> Unit = { },
    onNavigateToChangeEmail: () -> Unit = { },
    onNavigateToChangePhone: () -> Unit = { },
    onConvertToCitizen: () -> Unit = { },
    onDisassociateVehicle: () -> Unit = { },
    onNavigateToVehicleAssociation: () -> Unit = { }
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MenuButton(
            icon = Icons.Outlined.Person,
            title = "Información Personal",
            onClick = { onNavigateToPersonalInfo() }
        )

        MenuButton(
            icon = Icons.Outlined.ContactMail,
            title = "Cambiar de Correo Electrónico",
            onClick = { onNavigateToChangeEmail() }
        )

        MenuButton(
            icon = Icons.Outlined.Phone,
            title = "Cambiar de Teléfono",
            onClick = { onNavigateToChangePhone() },
        )

        // MenuButton para obtener el rol de ciudadano
        MenuButton(
            icon = Icons.Outlined.ChangeCircle,
            title = "Convertirse en Ciudadano",
            onClick = { onConvertToCitizen() }
        )


        // Asociar vehiculo a conductor
        MenuButton(
            icon = Icons.Outlined.LocalTaxi,
            title = "Asociar Vehículo",
            onClick = { onNavigateToVehicleAssociation() }
        )

        // Desasociar vehiculo de conductor
        MenuButton(
            icon = Icons.Outlined.TaxiAlert,
            title = "Desasociar Vehículo",
            onClick = { onDisassociateVehicle() }
        )

    }
}

@Composable
private fun MenuButton(
    icon: ImageVector,
    title: String,
    titleColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable (
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = ITaxCixPaletaColors.Blue1)
            ) { onClick() }
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ITaxCixPaletaColors.Blue1,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Float = 0f,
    stars: Int = 5,
    starsColor: Color = Color(0xFFFFC107)
) {
    Row(modifier = modifier) {
        for (i in 1..stars) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star
                else if (i <= rating + 0.5f) Icons.AutoMirrored.Filled.StarHalf
                else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = starsColor
            )
        }
    }
}