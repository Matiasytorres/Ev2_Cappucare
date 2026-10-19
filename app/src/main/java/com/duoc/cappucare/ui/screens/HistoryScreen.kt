package com.duoc.cappucare.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.cappucare.data.MovementEntity
import com.duoc.cappucare.data.MovementType
import com.duoc.cappucare.data.ProductEntity
import com.duoc.cappucare.ui.theme.AlertRedDark
import com.duoc.cappucare.ui.theme.BackgroundGray
import com.duoc.cappucare.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    products: List<ProductEntity>,
    movements: List<MovementEntity>,
    onMovementClick: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val dateTimeFormat = remember { SimpleDateFormat("dd-MM-yyyy/HH:mm", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    val productsBySku = remember(products) { products.associateBy { it.sku } }
    val categories = remember(products) {
        listOf("Todos") + products.map { it.category }.distinct().sorted()
    }

    val totalSold = movements
        .filter { it.type == MovementType.SALIDA }
        .sumOf { it.quantity }

    val filtered = movements.filter { m ->
        val product = productsBySku[m.productSku]
        val matchesCategory = selectedCategory == "Todos" || product?.category == selectedCategory
        val matchesSearch = search.isBlank() ||
                m.productSku.contains(search, ignoreCase = true) ||
                (product?.name?.contains(search, ignoreCase = true) == true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Historial", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Buscar insumos o códigos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Text("Historial de movimientos", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryBox("Total insumos vendidos", "$totalSold", Modifier.weight(1f))
                SummaryBox("Fecha", dayFormat.format(Date()), Modifier.weight(1f))
            }
        }

        item { Text("Filtrar por categoría", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        item { Text("Lista de movimientos", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        if (filtered.isEmpty()) {
            item { Text("No hay movimientos para mostrar", color = Color.Gray) }
        } else {
            items(filtered, key = { it.id }) { m ->
                val product = productsBySku[m.productSku]
                val isIngreso = m.type == MovementType.INGRESO

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMovementClick(m.productSku) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nombre:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(product?.name ?: m.productSku, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Código:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(m.productSku, fontSize = 14.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ubicación:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(product?.location ?: "-", fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Fecha y hora:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(dateTimeFormat.format(Date(m.date)), fontSize = 14.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Cantidad:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("${m.quantity}", fontSize = 14.sp)
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (isIngreso) Color(0xFF4CAF50) else AlertRedDark)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedButton(onClick = { onMovementClick(m.productSku) }) {
                                Text(if (isIngreso) "Ingreso" else "Venta", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SummaryBox(title: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, TealPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

