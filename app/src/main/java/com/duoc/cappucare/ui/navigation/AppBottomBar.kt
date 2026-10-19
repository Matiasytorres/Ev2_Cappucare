package com.duoc.cappucare.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.cappucare.ui.theme.TealPrimary

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppBottomBar(selectedRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(
        BottomItem(Routes.HOME, "Inicio", Icons.Filled.Home),
        BottomItem(Routes.INVENTORY, "Inventario", Icons.AutoMirrored.Filled.List),
        BottomItem(Routes.SCAN, "Escanear", Icons.Filled.QrCodeScanner),
        BottomItem(Routes.HISTORY, "Historial", Icons.Filled.History),
        BottomItem(Routes.PROFILE, "Mi Perfil", Icons.Filled.Person)
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    if (item.route == Routes.SCAN) {
                        Surface(
                            shape = CircleShape,
                            color = TealPrimary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(item.icon, contentDescription = item.label, tint = Color.White)
                            }
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealPrimary,
                    selectedTextColor = TealPrimary,
                    indicatorColor = TealPrimary.copy(alpha = 0.15f)
                )
            )
        }
    }
}