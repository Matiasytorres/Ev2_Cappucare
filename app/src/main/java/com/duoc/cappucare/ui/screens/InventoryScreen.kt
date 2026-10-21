package com.duoc.cappucare.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.cappucare.data.ProductEntity
import com.duoc.cappucare.ui.theme.AlertRed
import com.duoc.cappucare.ui.theme.AlertRedDark
import com.duoc.cappucare.ui.theme.BackgroundGray
import com.duoc.cappucare.ui.theme.StatusGreen
import com.duoc.cappucare.ui.theme.TealDark
import com.duoc.cappucare.ui.theme.TealPrimary
import java.text.NumberFormat
import java.util.Locale

private const val ALL_CATEGORIES = "Todos"

/**
 * Pantalla Inventario (mockup "Inventario").
 *
 * Secciones, de arriba hacia abajo:
 *  1. Encabezado con título e ícono
 *  2. Buscador (nombre, código SKU o código de barras)
 *  3. Resumen: total de insumos y productos con stock bajo
 *  4. Filtro por categoría (chips)
 *  5. Lista de productos con semáforo y botón de acción
 */
@Composable
fun InventoryScreen(
    products: List<ProductEntity>,
    onProductClick: (String) -> Unit,
    onAddProduct: () -> Unit,
    onRegisterSale: (String) -> Unit,
    onRequestRestock: (String) -> Unit
) {
    // Estado de la UI: sobrevive a rotaciones y a cambios de pestaña
    var search by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(ALL_CATEGORIES) }

    // Valores derivados: se recalculan solo cuando cambian sus dependencias
    val categories = remember(products) {
        listOf(ALL_CATEGORIES) + products.map { it.category }.distinct().sorted()
    }
    // Si la categoría elegida desaparece (ej: se borró su último producto), volvemos a "Todos"
    val activeCategory = if (selectedCategory in categories) selectedCategory else ALL_CATEGORIES

    val totalUnits = remember(products) { products.sumOf { it.stock } }
    val lowStockCount = remember(products) { products.count { it.stock <= it.minStock } }

    val filtered = remember(products, search, activeCategory) {
        products.filter { p ->
            val matchesCategory = activeCategory == ALL_CATEGORIES || p.category == activeCategory
            val matchesSearch = search.isBlank() ||
                    p.name.contains(search, ignoreCase = true) ||
                    p.sku.contains(search, ignoreCase = true) ||
                    p.barcode.contains(search, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val numberFormat = remember { NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Encabezado
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Inventario", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = TealDark,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // 2. Buscador
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Buscar insumos o códigos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClick = { search = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = TealPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 3. Resumen
        item { Text("Resumen de inventario", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InventorySummaryCard(
                    title = "Total insumos",
                    value = numberFormat.format(totalUnits),
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f)
                )
                InventorySummaryCard(
                    title = "Bajo stock",
                    value = "$lowStockCount",
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Filtro por categoría
        item { Text("Filtrar por categoría", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = activeCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            selectedContainerColor = TealPrimary.copy(alpha = 0.35f),
                            selectedLabelColor = TealDark
                        )
                    )
                }
            }
        }

        // 5. Encabezado de la lista + botón Agregar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lista de productos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onAddProduct,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar producto", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Productos (o estado vacío)
        if (filtered.isEmpty()) {
            item {
                Text(
                    text = if (products.isEmpty()) {
                        "No hay productos registrados"
                    } else {
                        "Ningún producto coincide con tu búsqueda"
                    },
                    color = Color.Gray
                )
            }
        } else {
            items(filtered, key = { it.sku }) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product.sku) },
                    onRegisterSale = { onRegisterSale(product.sku) },
                    onRequestRestock = { onRequestRestock(product.sku) }
                )
            }
        }
    }
}

/** Tarjeta con borde turquesa: título, valor grande e ícono. */
@Composable
private fun InventorySummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, TealPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TealPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/** Tarjeta de producto con semáforo (verde = normal, rojo = bajo) y botón de acción. */
@Composable
private fun ProductCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onRegisterSale: () -> Unit,
    onRequestRestock: () -> Unit
) {
    val isLow = product.stock <= product.minStock
    val statusColor = if (isLow) AlertRedDark else StatusGreen
    val statusLabel = when {
        product.stock == 0 -> "Agotado"
        isLow -> "Bajo"
        else -> "Normal"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isLow) AlertRed else Color(0xFFDDE3E8)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nombre + semáforo
            Row(verticalAlignment = Alignment.Top) {
                LabeledValue(label = "Nombre:", value = product.name, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                        .semantics { contentDescription = "Estado de stock: $statusLabel" }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Código + ubicación
            Row {
                LabeledValue(label = "Código:", value = product.sku, modifier = Modifier.weight(1f))
                LabeledValue(label = "Ubicación:", value = product.location, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stock + botón de acción
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stock: $statusLabel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLow) AlertRedDark else Color.Unspecified
                    )
                    Text("${product.stock} ${product.unit} (Mín: ${product.minStock})", fontSize = 15.sp)
                }
                OutlinedButton(
                    onClick = if (isLow) onRequestRestock else onRegisterSale,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isLow) "Pedir reposición" else "Registrar venta",
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/** Par "Etiqueta:" (pequeña, en negrita) + valor debajo. */
@Composable
private fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryScreenPreview() {
    InventoryScreen(
        products = listOf(
            ProductEntity("CAP-MED-012", "Mascarilla Quirúrgica (KN95)", "Quirúrgicos", 150, 20, "Estante A-2", "08/2027", unit = "Cajas"),
            ProductEntity("CAP-GUA-004", "Guantes de Nitrilo (Talla M)", "Desechables", 4, 15, "Estante B-1", "03/2028", unit = "Cajas"),
            ProductEntity("CAP-MED-001", "Paracetamol 500 mg", "Fármacos", 28, 10, "Estante B-4", "12/2026", unit = "Cajas")
        ),
        onProductClick = {},
        onAddProduct = {},
        onRegisterSale = {},
        onRequestRestock = {}
    )
}