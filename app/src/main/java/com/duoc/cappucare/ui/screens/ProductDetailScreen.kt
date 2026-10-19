package com.duoc.cappucare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.cappucare.data.MovementEntity
import com.duoc.cappucare.data.MovementType
import com.duoc.cappucare.data.ProductEntity
import com.duoc.cappucare.ui.theme.BackgroundGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductDetailScreen(
    product: ProductEntity?,
    movements: List<MovementEntity>,
    message: String?,
    onRegister: (MovementType, Int, String, String) -> Unit,
    onClearMessage: () -> Unit,
    onBack: () -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var responsible by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")) }

    fun submit(type: MovementType) {
        onRegister(type, quantity.toIntOrNull() ?: 0, reason, responsible)
        quantity = ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Detalle de producto", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (product == null) {
            item { Text("Producto no encontrado", color = Color.Gray) }
            return@LazyColumn
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("SKU: ${product.sku}", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Categoría: ${product.category}")
                    Text("Ubicación: ${product.location}")
                    Text("Vencimiento: ${product.expiryDate}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Stock: ${product.stock} (mínimo ${product.minStock})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registrar movimiento", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = responsible,
                        onValueChange = { responsible = it },
                        label = { Text("Responsable") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { submit(MovementType.INGRESO) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Ingreso") }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { submit(MovementType.SALIDA) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Salida (venta)") }
                    }
                    if (message != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(message, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            TextButton(onClick = onClearMessage) { Text("OK") }
                        }
                    }
                }
            }
        }

        item { Text("Historial de movimientos", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

        if (movements.isEmpty()) {
            item { Text("Sin movimientos registrados", color = Color.Gray) }
        } else {
            items(movements, key = { it.id }) { m ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(12.dp)) {
                        val label = if (m.type == MovementType.INGRESO) "Ingreso" else "Salida"
                        Text("$label de ${m.quantity} unidades", fontWeight = FontWeight.Bold)
                        Text(dateFormat.format(Date(m.date)), fontSize = 12.sp, color = Color.Gray)
                        if (m.reason.isNotBlank()) Text("Motivo: ${m.reason}", fontSize = 13.sp)
                        if (m.responsible.isNotBlank()) Text("Responsable: ${m.responsible}", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}