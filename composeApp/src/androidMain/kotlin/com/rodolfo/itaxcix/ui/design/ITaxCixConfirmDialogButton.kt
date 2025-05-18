package com.rodolfo.itaxcix.ui.design

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Composable
fun ITaxCixConfirmDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Sí, confirmar",
    dismissButtonText: String = "Cancelar",
    containerColor: Color = ITaxCixPaletaColors.Background,
    titleColor: Color = ITaxCixPaletaColors.Blue1,
    textColor: Color = Color.DarkGray,
    confirmButtonColor: Color = ITaxCixPaletaColors.Blue2,
    confirmTextColor: Color = Color.White,
    dismissButtonColor: Color = ITaxCixPaletaColors.Blue1
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
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = confirmButtonColor,
                        contentColor = confirmTextColor
                    )
                ) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = dismissButtonColor
                    )
                ) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}