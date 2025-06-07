package com.rodolfo.itaxcix.ui.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

@Composable
fun ITaxCixPermissionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    permissionTitle: String = "Permiso requerido",
    permissionDescription: String,
    permissionReason: String,
    permissionIcon: ImageVector,
    confirmButtonText: String = "Permitir",
    dismissButtonText: String = "Cancelar",
    containerColor: Color = ITaxCixPaletaColors.Background,
    titleColor: Color = ITaxCixPaletaColors.Blue1,
    textColor: Color = Color.DarkGray,
    iconTint: Color = ITaxCixPaletaColors.Blue2,
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
                    text = permissionTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = permissionDescription,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = permissionReason,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = permissionIcon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = confirmButtonColor,
                        contentColor = confirmTextColor
                    )
                ) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = dismissButtonColor
                    )
                ) {
                    Text(dismissButtonText)
                }
            }
        )
    }
}