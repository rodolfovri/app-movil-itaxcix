package com.rodolfo.itaxcix.feature.driver.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import androidx.core.net.toFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.data.local.UserData
import com.rodolfo.itaxcix.feature.driver.profile.driverProfileViewModel.DriverProfileViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import com.rodolfo.itaxcix.utils.ImageUtils
import kotlinx.coroutines.launch

@Preview
@Composable
fun DriverProfileScreenPreview() {
    DriverProfileScreen()
}

@Composable
fun DriverProfileScreen(
    viewModel: DriverProfileViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val isLoadingProfileImage by viewModel.isLoadingProfileImage.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isLoading = uploadState is DriverProfileViewModel.UploadState.Loading
    val isSuccess = uploadState is DriverProfileViewModel.UploadState.Success

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
                    // Opcional: Mostrar error al usuario
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ITaxCixPaletaColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ProfileHeader(
            userData = userData,
            isLoadingProfileImage = isLoadingProfileImage,
            onImageClick = { galleryLauncher.launch("image/*") }
        )
        Spacer(modifier = Modifier.height(24.dp))
        MenuButtons()
    }

    ITaxCixProgressRequest(
        isVisible = isLoading || isSuccess,
        isSuccess = isSuccess,
        loadingTitle = "Subiendo foto",
        successTitle = "¡Foto actualizada!",
        loadingMessage = "Estamos procesando tu nueva foto de perfil...",
        successMessage = "Tu foto de perfil ha sido actualizada correctamente"
    )
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
private fun MenuButtons() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MenuButton(
            icon = Icons.Outlined.Person,
            title = "Información Personal",
            onClick = { /* Navegar a información personal */ }
        )

        MenuButton(
            icon = Icons.Outlined.PersonAdd,
            title = "Cambiar de Contacto",
            onClick = { /* Navegar a cambiar contacto */ }
        )

        MenuButton(
            icon = Icons.Outlined.DirectionsCar,
            title = "Cambiar de Carro",
            onClick = { /* Navegar a cambiar carro */ }
        )

        MenuButton(
            icon = Icons.Outlined.Favorite,
            title = "Tus Favoritos",
            onClick = { /* Navegar a favoritos */ }
        )

        MenuButton(
            icon = Icons.Outlined.Payment,
            title = "Método de Pago",
            onClick = { /* Navegar a métodos de pago */ }
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