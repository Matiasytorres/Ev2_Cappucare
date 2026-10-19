package com.duoc.cappucare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.cappucare.data.MovementEntity
import com.duoc.cappucare.data.ProductEntity
import com.duoc.cappucare.ui.theme.*

@Composable
fun HomeScreen(
    products: List<ProductEntity>,
    lowStockList: List<ProductEntity>,
    movements: List<MovementEntity>,
    onNavigateToScan: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("¡Hola, Laura!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Encargada de bodega", fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (lowStockList.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AlertRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRedDark)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${lowStockList.size} productos bajo el stock mínimo",
                            fontWeight = FontWeight.Bold,
                            color = AlertRedDark
                        )
                    }
                    Button(
                        onClick = onNavigateToInventory,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Ver detalles", color = AlertRedDark, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Resumen de inventario", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryCard("Total insumos", "${products.sumOf { it.stock }}", Icons.Default.MedicalServices, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            SummaryCard("Bajo stock", "${lowStockList.size}", Icons.Default.Warning, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            SummaryCard("Movimientos", "${movements.size}", Icons.Default.SwapVert, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Accesos rápidos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            QuickActionButton("Escanear", Icons.Default.QrCodeScanner, onNavigateToScan, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            QuickActionButton("Nuevo Ingreso", Icons.Default.AddBox, onNavigateToAdd, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            QuickActionButton("Inventario", Icons.Default.List, onNavigateToInventory, Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun QuickActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = TealPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = Color.Black)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockProducts = listOf(
        ProductEntity("1", "Paracetamol", "Fármacos", 5, 10, "Bodega A", "12/2026")
    )
    HomeScreen(
        products = mockProducts,
        lowStockList = mockProducts,
        movements = emptyList(),
        onNavigateToScan = {},
        onNavigateToAdd = {},
        onNavigateToInventory = {}
    )
}