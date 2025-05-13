import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
    val route: String
)

val driverDrawerItems = listOf(
    DrawerItem("Perfil", Icons.Default.Person, "driverProfile"),
    DrawerItem("Disponibilidad", Icons.Default.AccessTime, "driverAvailability"),
    DrawerItem("Historial", Icons.Default.History, "driverHistory"),
    DrawerItem("Cerrar sesión", Icons.Default.ExitToApp, "logout")
)

@Composable
fun DriverDrawerContent(onItemClick: (String) -> Unit) {
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
                Divider(color = ITaxCixPaletaColors.Blue3, thickness = 1.dp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Items del drawer
        for (item in driverDrawerItems) {
            DrawerItemRow(
                item = item,
                onItemClick = onItemClick
            )
            if (item != driverDrawerItems.last()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DrawerItemRow(item: DrawerItem, onItemClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onItemClick(item.route) }
            .background(Color.Transparent)
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