package com.rodolfo.itaxcix.feature.citizen.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodolfo.itaxcix.R
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors
import com.rodolfo.itaxcix.utils.ImageUtils

data class CitizenDrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val requiredPermission: String? = null
)

val citizenDrawerItems = listOf(
    CitizenDrawerItem("Inicio", Icons.Outlined.Home, CitizenRoutes.HOME),
    CitizenDrawerItem("Perfil", Icons.Default.PersonOutline, CitizenRoutes.PROFILE),
    CitizenDrawerItem("Historial", Icons.Default.History, CitizenRoutes.HISTORY),
    CitizenDrawerItem("Cerrar sesión", Icons.AutoMirrored.Filled.ExitToApp, "logout") // Sin permiso requerido
)

@Composable
fun CitizenDrawerContent(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    userPermissions: List<String> = emptyList(),
    userName: String = "",
    userRole: String = "Ciudadano",
    userImage: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RectangleShape)
                .background(Color.White)
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ico_itaxcix),
                    contentDescription = "Logo iTaxCix",
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "iTaxCix",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ITaxCixPaletaColors.Blue1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Elementos del menú (parte central)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            val visibleItems = citizenDrawerItems.filter { item ->
                item.requiredPermission == null || userPermissions.contains(item.requiredPermission)
            }

            for (item in visibleItems) {
                DrawerItemRow(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onItemClick = onItemClick
                )
                if (item != visibleItems.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Footer con información del usuario - con sombra
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RectangleShape, clip = false, spotColor = Color.Black.copy(alpha = 0.1f))
                .background(Color.White)
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del usuario
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3F51B5)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userImage.isNullOrEmpty()) {
                        val bitmap = ImageUtils.decodeBase64ToBitmap(userImage)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_profile_image),
                                contentDescription = "Perfil predeterminado",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_image),
                            contentDescription = "Perfil predeterminado",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = userName.ifEmpty { "Usuario" },
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = userRole,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItemRow(
    item: CitizenDrawerItem,
    isSelected: Boolean,
    onItemClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onItemClick(item.route) }
            .background(
                if (isSelected) ITaxCixPaletaColors.Blue3.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = ITaxCixPaletaColors.Blue1
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}