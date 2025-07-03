package com.rodolfo.itaxcix.feature.citizen.profile

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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
import com.rodolfo.itaxcix.feature.citizen.profile.citizenProfileViewModel.CitizenProfileViewModel
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import com.rodolfo.itaxcix.utils.ImageUtils
import kotlinx.coroutines.launch

@Preview
@Composable
fun CitizenProfileScreenPreview() {
    CitizenProfileScreen()
}

@Composable
fun CitizenProfileScreen(
    viewModel: CitizenProfileViewModel = hiltViewModel(),
    onNavigateToPersonalInfo: () -> Unit = { },
    onNavigateToChangeEmail: () -> Unit = { },
    onNavigateToChangePhone: () -> Unit = { },
    onNavigateToCitizenToDriver: () -> Unit = { }
) {
    val userData by viewModel.userData.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val isLoadingProfileImage by viewModel.isLoadingProfileImage.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isLoading = uploadState is CitizenProfileViewModel.UploadState.Loading
    val isSuccess = uploadState is CitizenProfileViewModel.UploadState.Success

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
                    Log.e("CitizenProfile", "Error al procesar imagen: ${e.message}")
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
        MenuButtons(
            onNavigateToPersonalInfo = onNavigateToPersonalInfo,
            onNavigateToChangeEmail = onNavigateToChangeEmail,
            onNavigateToChangePhone = onNavigateToChangePhone,
            onNavigateToCitizenToDriver = onNavigateToCitizenToDriver
        )
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
            text = "${userData?.firstName ?: ""} ${userData?.lastName ?: ""}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userData?.roles?.firstOrNull() ?: "Ciudadano",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .background(
                    color = ITaxCixPaletaColors.Blue1,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

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
    onNavigateToCitizenToDriver: () -> Unit = { }
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MenuButton(
            icon = Icons.Outlined.Person,
            title = "Información Personal",
            onClick = { onNavigateToPersonalInfo() },
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

        MenuButton(
            icon = Icons.Outlined.ChangeCircle,
            title = "Convertir a Conductor",
            onClick = { onNavigateToCitizenToDriver() },
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
            .clickable(
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