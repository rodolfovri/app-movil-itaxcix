import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val requiredPermission: String? = null
)

val driverDrawerItems = listOf(
    DrawerItem("Inicio", Icons.Default.Home, "driverHome", "INICIO CONDUCTOR"),
    DrawerItem("Perfil", Icons.Default.Person, "driverProfile", "PERFIL CONDUCTOR"),
    DrawerItem("Historial", Icons.Default.History, "driverHistory", "HISTORIAL CONDUCTOR"),
    DrawerItem("Cerrar sesión", Icons.AutoMirrored.Filled.ExitToApp, "logout")
)

@Composable
fun DriverDrawerContent(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    userPermissions: List<String> = emptyList() // Lista de permisos del usuario
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
                    text = "Panel del Conductor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ITaxCixPaletaColors.Blue1
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 1.dp, color = ITaxCixPaletaColors.Blue3)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val visibleItems = driverDrawerItems.filter { item ->
            item.requiredPermission == null || userPermissions.contains(item.requiredPermission)
        }

        // Mostrar los elementos del drawer
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
}

@Composable
fun DrawerItemRow(
    item: DrawerItem,
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