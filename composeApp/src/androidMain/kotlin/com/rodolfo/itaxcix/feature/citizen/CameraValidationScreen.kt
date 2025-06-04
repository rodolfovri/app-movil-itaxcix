package com.rodolfo.itaxcix.feature.citizen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.rodolfo.itaxcix.feature.auth.viewmodel.CameraValidationViewModel
import com.rodolfo.itaxcix.services.camera.CameraManager
import com.rodolfo.itaxcix.services.camera.CameraView
import com.rodolfo.itaxcix.services.camera.analyzers.FaceDetectionAnalyzer
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.ui.design.ITaxCixProgressRequest
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraValidationScreen(
    viewModel: CameraValidationViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onValidationSuccess: (Int?) -> Unit = { _ -> }
) {
    // Estados existentes
    val validationState by viewModel.validationState.collectAsState()
    val validationProgress by viewModel.validationProgress.collectAsState()
    val faceDetected by viewModel.faceDetected.collectAsState()
    val personId by viewModel.personId.collectAsState()

    var cameraManager by remember { mutableStateOf<CameraManager?>(null) }
    var currentCameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA) }
    var capturedPhotoFile by remember { mutableStateOf<File?>(null) }
    var photoTaken by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessSnackbar by remember { mutableStateOf(false) }

    // Contexto de corrutina para usar en los callbacks
    val context = LocalContext.current

    // Indicadores para el ITaxCixProgressRequest
    val isLoading = validationState is CameraValidationViewModel.ValidationState.BiometricValidating
    val isSuccess = validationState is CameraValidationViewModel.ValidationState.BiometricSuccess

    // Estado para permisos de cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Función para reiniciar la validación facial
    fun resetValidation() {
        photoTaken = false
        capturedPhotoFile = null
        // Reiniciar el ViewModel
        viewModel.resetValidationManually()

        // Reiniciar la cámara
        cameraManager?.stopCamera()
        cameraManager?.startCamera(
            cameraSelector = currentCameraSelector,
            imageAnalyzer = FaceDetectionAnalyzer { faces, validationState ->
                viewModel.onFaceDetected(faces.isNotEmpty())
                viewModel.processFaceAnalysis(faces, validationState)
            }
        )
    }

    // Reaccionamos cuando el estado de validación cambia a Success
    LaunchedEffect(validationState) {
        Log.d("CameraValidation", "ValidationState changed to: $validationState")

        when (val state = validationState) {
            is CameraValidationViewModel.ValidationState.Success -> {
                Log.d("CameraValidation", "Facial validation success, photoTaken: $photoTaken")
                if (!photoTaken) {
                    cameraManager?.takePhoto(
                        executor = ContextCompat.getMainExecutor(context),
                        onPhotoTaken = { file ->
                            Log.d("CameraValidation", "Photo taken successfully: ${file.absolutePath}")
                            capturedPhotoFile = file
                            photoTaken = true
                        },
                        onError = { exception ->
                            Log.e("CameraValidation", "Error taking photo", exception)
                            photoTaken = true
                        }
                    )
                }
            }
            is CameraValidationViewModel.ValidationState.BiometricSuccess -> {
                delay(1800) // Esperar 1.8 segundos para mostrar el mensaje de éxito
                onValidationSuccess(personId)
                viewModel.onSuccessShown()
            }
            is CameraValidationViewModel.ValidationState.BiometricError -> {
                isSuccessSnackbar = false
                snackbarHostState.showSnackbar(
                    message = "Error: ${state.message}",
                    duration = SnackbarDuration.Short
                )
                viewModel.onErrorShown()
                resetValidation()
            }
            is CameraValidationViewModel.ValidationState.BiometricValidating -> {
                Log.d("CameraValidation", "Biometric validation in progress...")
            }
            else -> {
                Log.d("CameraValidation", "Other state: $validationState")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            topBar = {
                TopAppBar(
                    title = { Text("Validación facial") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = ITaxCixPaletaColors.Blue1
                            )
                        }
                    },
                    actions = {
                        if (hasCameraPermission && validationState !is CameraValidationViewModel.ValidationState.Success) {
                            IconButton(onClick = {
                                cameraManager?.switchCamera()
                                currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Cameraswitch,
                                    contentDescription = "Cambiar cámara",
                                    tint = ITaxCixPaletaColors.Blue1
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            containerColor = Color.White
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (hasCameraPermission) {
                    // Vista de la cámara
                    CameraView(
                        modifier = Modifier.fillMaxSize(),
                        cameraSelector = currentCameraSelector,
                        imageAnalyzer = FaceDetectionAnalyzer { faces, validationState ->
                            viewModel.onFaceDetected(faces.isNotEmpty())
                            viewModel.processFaceAnalysis(faces, validationState)
                        },
                        hasCameraPermission = true,
                        onCameraReady = { manager ->
                            cameraManager = manager
                        }
                    )

                    // Indicador del estado de detección facial
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = when (validationState) {
                                    is CameraValidationViewModel.ValidationState.Waiting ->
                                        if (faceDetected) "Rostro detectado, iniciando validación"
                                        else "Coloque su rostro frente a la cámara"
                                    is CameraValidationViewModel.ValidationState.Validating ->
                                        "Validando su identidad..."
                                    is CameraValidationViewModel.ValidationState.Success ->
                                        if (photoTaken) "Fotografía capturada correctamente" else "¡Validación exitosa! Capturando fotografía..."
                                    is CameraValidationViewModel.ValidationState.BiometricError ->
                                        "Error en la validación biométrica"
                                    is CameraValidationViewModel.ValidationState.BiometricSuccess ->
                                        "Validación biométrica exitosa"
                                    CameraValidationViewModel.ValidationState.BiometricValidating ->
                                        "Validando datos biométricos..."
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            when (validationState) {
                                is CameraValidationViewModel.ValidationState.Validating -> {
                                    LinearProgressIndicator(
                                        progress = { validationProgress },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = ITaxCixPaletaColors.Blue1
                                    )
                                }
                                is CameraValidationViewModel.ValidationState.Success -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Validación exitosa",
                                        tint = Color.Green,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                else -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = "Rostro",
                                            tint = if (faceDetected) Color.Green else Color.Red,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Text(
                                            text = if (faceDetected) "Rostro detectado" else "No se detecta rostro",
                                            color = if (faceDetected) Color.Green else Color.Red,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Menú de opciones (visible después de validación exitosa y foto tomada)
                    if (validationState is CameraValidationViewModel.ValidationState.Success && photoTaken) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                                .fillMaxWidth(0.9f),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Validación biométrica completada",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Botón para continuar
                                Button(
                                    onClick = {
                                        Log.d("CameraValidation", "Continue button clicked")
                                        Log.d("CameraValidation", "PersonId: $personId")
                                        Log.d("CameraValidation", "CapturedPhotoFile: ${capturedPhotoFile?.absolutePath}")

                                        if (capturedPhotoFile != null) {
                                            Log.d("CameraValidation", "Starting biometric validation...")
                                            cameraManager?.stopCamera()

                                            viewModel.validateBiometricImage(capturedPhotoFile)
                                        } else {
                                            Log.e("CameraValidation", "No hay foto para validar")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ITaxCixPaletaColors.Blue1
                                    )
                                ) {
                                    Text(
                                        text = "Continuar con el registro",
                                        style = MaterialTheme.typography.labelLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Botón para volver a realizar la validación
                                Button(
                                    onClick = {
                                        resetValidation()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ITaxCixPaletaColors.Blue3
                                    )
                                ) {
                                    Text(
                                        text = "Volver a identificación facial",
                                        style = MaterialTheme.typography.labelLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Vista cuando no hay permiso de cámara
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Cámara",
                            modifier = Modifier.size(80.dp),
                            tint = ITaxCixPaletaColors.Blue1
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Validación facial",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Para continuar con la validación de su identidad, necesitamos acceso a la cámara de su dispositivo.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(56.dp),
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ITaxCixPaletaColors.Blue1,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Cámara",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Permitir acceso a la cámara",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        ITaxCixProgressRequest(
            isVisible = isLoading || isSuccess,
            isSuccess = isSuccess,
            loadingTitle = "Validando Biometría",
            successTitle = "Validación Exitosa",
            loadingMessage = "Verificando tu identidad con nuestros servidores...",
            successMessage = "Identidad verificada correctamente"
        )
    }
}