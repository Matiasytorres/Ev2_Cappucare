package com.duoc.cappucare.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.cappucare.data.LotEntity
import com.duoc.cappucare.data.MovementEntity
import com.duoc.cappucare.data.MovementType
import com.duoc.cappucare.data.ProductEntity
import com.duoc.cappucare.model.ExpiryStatus
import com.duoc.cappucare.model.ExpiryUtils
import com.duoc.cappucare.ui.theme.AlertRedDark
import com.duoc.cappucare.ui.theme.BackgroundGray
import com.duoc.cappucare.ui.theme.StatusGreen
import com.duoc.cappucare.ui.theme.StatusOrange
import com.duoc.cappucare.ui.theme.TealDark
import com.duoc.cappucare.ui.theme.TealPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla Detalle de producto (mockup "Detalle producto").
 * Arriba: ficha, información general, lotes e información médica.
 * Abajo: el formulario de movimientos de siempre (lo reemplazará la tarjeta MovementDialog).
 */
@Composable
fun ProductDetailScreen(
    product: ProductEntity?,
    lots: List<LotEntity>,
    movements: List<MovementEntity>,
    message: String?,
    onRegister: (MovementType, Int, String, String) -> Unit,
    onClearMessage: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var responsible by remember { mutableStateOf("") }
    var lotsExpanded by rememberSaveable { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val locale = remember { Locale.forLanguageTag("es-CL") }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", locale) }
    val moneyFormat = remember { NumberFormat.getIntegerInstance(locale) }

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
        // ---------- Barra superior ----------
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = "Detalle de producto",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (product != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar producto", tint = TealDark)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar producto", tint = AlertRedDark)
                    }
                }
            }
        }

        if (product == null) {
            item { Text("Producto no encontrado", color = Color.Gray) }
            return@LazyColumn
        }

        // ---------- Ficha: imagen + nombre + estado + SKU ----------
        item {
            val (badgeText, badgeColor) = when {
                product.stock == 0 -> "Agotado" to AlertRedDark
                product.stock <= product.minStock -> "Stock bajo" to StatusOrange
                else -> "En stock" to StatusGreen
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFDDE3E8), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.imageUri != null) {
                        AsyncImage(
                            model = product.imageUri,
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = badgeColor) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SKU: ${product.sku}", fontSize = 14.sp, color = Color.DarkGray)
                }
            }
        }

        // ---------- Información general ----------
        item { Text("Información general", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(Icons.Default.Category, "Categoría", product.category, Modifier.weight(1f).fillMaxHeight())
                InfoChip(
                    Icons.Default.Sell, "Precio de venta", "$${moneyFormat.format(product.price)}",
                    Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(
                    Icons.Default.Inventory, "Stock (mínimo ${product.minStock})", "${product.stock} ${product.unit}",
                    Modifier.weight(1f).fillMaxHeight()
                )
                InfoChip(
                    Icons.Default.Business, "Laboratorio / Marca", product.brand.ifBlank { "No registrado" },
                    Modifier.weight(1f).fillMaxHeight()
                )
            }
        }

        // ---------- Lotes y vencimientos (desplegable) ----------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, TealPrimary),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { lotsExpanded = !lotsExpanded }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HISTORIAL DE LOTES Y VENCIMIENTOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (lotsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (lotsExpanded) "Contraer" else "Expandir"
                        )
                    }
                    AnimatedVisibility(visible = lotsExpanded) {
                        LotsTable(lots)
                    }
                }
            }
        }

        // ---------- Información médica ----------
        item { Text("Información médica", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MedicalBlock(Icons.Default.Description, "Indicaciones", product.indications)
                    MedicalBlock(Icons.Default.Medication, "Dosis recomendada", product.dosage)
                    MedicalBlock(Icons.Default.Warning, "Contraindicaciones", product.contraindications)
                }
            }
        }

        // ---------- Registrar movimiento (sin cambios) ----------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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

        // ---------- Historial de movimientos (sin cambios) ----------
        item { Text("Historial de movimientos", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

        if (movements.isEmpty()) {
            item { Text("Sin movimientos registrados", color = Color.Gray) }
        } else {
            items(movements, key = { it.id }) { m ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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

    // ---------- Confirmación de eliminación ----------
    if (showDeleteDialog && product != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar producto?") },
            text = {
                Text(
                    "Se eliminará \"${product.name}\" junto con sus lotes y su historial de movimientos. " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Eliminar", color = AlertRedDark, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// =====================================================================
//  Componentes
// =====================================================================

/** Recuadro con borde turquesa: ícono + etiqueta pequeña + valor. */
@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, TealPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 11.sp, color = Color.Gray)
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Tabla: Lote | Caducidad | Cantidad | Ubicación. La caducidad se pinta según su estado. */
@Composable
private fun LotsTable(lots: List<LotEntity>) {
    Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
        if (lots.isEmpty()) {
            Text("Sin lotes registrados", color = Color.Gray, fontSize = 14.sp)
            return@Column
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TableCell("Lote", Modifier.weight(1f), bold = true)
            TableCell("Caducidad", Modifier.weight(1f), bold = true)
            TableCell("Cantidad", Modifier.weight(1f), bold = true)
            TableCell("Ubicación", Modifier.weight(1.2f), bold = true)
        }
        lots.forEach { lot ->
            HorizontalDivider()
            val expiryColor = when (ExpiryUtils.status(lot.expiryDate)) {
                ExpiryStatus.EXPIRED -> AlertRedDark
                ExpiryStatus.SOON -> StatusOrange
                else -> Color.Unspecified
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                TableCell(lot.lotNumber, Modifier.weight(1f))
                TableCell(lot.expiryDate, Modifier.weight(1f), color = expiryColor)
                TableCell("${lot.quantity}", Modifier.weight(1f))
                TableCell(lot.location, Modifier.weight(1.2f))
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        modifier = modifier.padding(vertical = 8.dp, horizontal = 2.dp)
    )
}

@Composable
private fun MedicalBlock(icon: ImageVector, title: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = TealDark, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = text.ifBlank { "No registrada" },
                fontSize = 14.sp,
                color = if (text.isBlank()) Color.Gray else Color.Unspecified
            )
        }
    }
}