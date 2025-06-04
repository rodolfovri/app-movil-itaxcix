package com.rodolfo.itaxcix.ui.design

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.rodolfo.itaxcix.data.local.UserData
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

@Composable
fun ITaxCixInactivityDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    title: String = "Sesión finalizada",
    message: String = "Su sesión ha finalizado debido a inactividad. Por favor, inicie sesión nuevamente.",
    buttonText: String = "Aceptar",
    containerColor: Color = ITaxCixPaletaColors.Background,
    titleColor: Color = ITaxCixPaletaColors.Blue1,
    textColor: Color = Color.DarkGray,
    buttonColor: Color = ITaxCixPaletaColors.Blue1,
    buttonTextColor: Color = Color.White
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = containerColor,
            titleContentColor = titleColor,
            textContentColor = textColor,
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = buttonTextColor
                    )
                ) {
                    Text(buttonText)
                }
            }
        )
    }
}

@Composable
fun ITaxCixInactivityHandler(
    timeoutMinutes: Int = 15,
    userData: UserData?, // Agregar parámetro para verificar si está logueado
    onInactivityTimeout: () -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var lastActiveTimestamp by remember { mutableStateOf(Clock.System.now()) }
    var wasInBackground by remember { mutableStateOf(false) }

    // Solo ejecutar el handler si el usuario está logueado
    if (userData != null) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        lastActiveTimestamp = Clock.System.now()
                        wasInBackground = true
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        if (wasInBackground) {
                            val currentTime = Clock.System.now()
                            val inactiveTime = currentTime - lastActiveTimestamp

                            // Si estuvo inactivo más del tiempo configurado
                            if (inactiveTime > timeoutMinutes.minutes) {
                                onInactivityTimeout()
                            }

                            wasInBackground = false
                        }
                        lastActiveTimestamp = Clock.System.now()
                    }
                    else -> { /* No hacer nada para otros eventos */ }
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}