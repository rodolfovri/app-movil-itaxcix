package com.rodolfo.itaxcix.feature.citizen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodolfo.itaxcix.ui.ITaxCixPaletaColors

data class CitizenDrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val citizenDrawerItems = listOf(
    CitizenDrawerItem("Inicio", Icons.Default.Home, "citizenHome"),
    CitizenDrawerItem("Perfil", Icons.Default.Person, "citizenProfile"),
    CitizenDrawerItem("Historial", Icons.Default.History, "citizenHistory"),
    CitizenDrawerItem("Cerrar sesión", Icons.Default.ExitToApp, "logout")
)

@Composable
fun CitizenDrawerContent(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
            .padding(vertical = 16.dp)
    ) {
        // Header del drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Panel del Ciudadano",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ITaxCixPaletaColors.Blue1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = ITaxCixPaletaColors.Blue3, thickness = 1.dp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Elementos del drawer
        for (item in citizenDrawerItems) {
            DrawerItemRow(
                item = item,
                isSelected = currentRoute == item.route,
                onItemClick = onItemClick
            )
            if (item != citizenDrawerItems.last()) {
                Spacer(modifier = Modifier.height(8.dp))
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