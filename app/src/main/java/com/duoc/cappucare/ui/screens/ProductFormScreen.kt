package com.duoc.cappucare.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.duoc.cappucare.model.ExpiryUtils
import com.duoc.cappucare.model.ProductOptions
import com.duoc.cappucare.ui.theme.BackgroundGray
import com.duoc.cappucare.ui.theme.TealDark
import com.duoc.cappucare.ui.theme.TealPrimary
import com.duoc.cappucare.viewmodel.ProductFormViewModel

/**
 * Pantalla Agregar/Editar producto (mockup "Agregar / editar producto").
 * Sirve para tres casos, según los argumentos con que se navegue:
 *  - nuevo producto
 *  - nuevo producto con código de barras precargado (viene del escáner)
 *  - editar un producto existente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    viewModel: ProductFormViewModel,
    onClose: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val errors by viewModel.errors.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    if (state.loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = TealPrimary) }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ---------- Encabezado ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = if (state.isEdit) "Editar producto" else "Agregar producto",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ---------- Foto (se conecta con la tarjeta ImagenInteligente) ----------
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, TealPrimary, CircleShape)
                        .clickable {
                            // TODO (tarjeta ImagenInteligente): abrir cámara o galería y guardar
                            // el resultado con viewModel.update { it.copy(imageUri = uri.toString()) }
                            Toast.makeText(context, "La foto se activa con la integración de cámara", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.imageUri != null) {
                        AsyncImage(
                            model = state.imageUri,
                            contentDescription = "Foto del producto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TealDark,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Toca para agregar foto", fontSize = 14.sp)
            }

            // ---------- Datos generales ----------
            SectionTitle("Datos generales")
            FormField(
                label = "Código SKU",
                value = state.sku,
                onValueChange = { v -> viewModel.update { it.copy(sku = v.uppercase().filter { c -> !c.isWhitespace() }.take(20)) } },
                error = errors.sku,
                placeholder = "Ej: CAP-MED-020",
                readOnly = state.isEdit
            )
            FormField(
                label = "Nombre del producto",
                value = state.name,
                onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                error = errors.name,
                placeholder = "Ej: Aspirina"
            )
            DropdownField(
                label = "Categoría del producto",
                value = state.category,
                options = ProductOptions.categories,
                error = errors.category,
                onSelected = { v -> viewModel.update { it.copy(category = v) } }
            )
            FormField(
                label = "Código de barras (opcional)",
                value = state.barcode,
                onValueChange = { v -> viewModel.update { it.copy(barcode = v.filter { c -> !c.isWhitespace() }.take(20)) } },
                error = errors.barcode,
                placeholder = "Ej: 1351256434"
            )
            FormField(
                label = "Precio de venta (CLP)",
                value = state.price,
                onValueChange = { v -> viewModel.update { it.copy(price = v.filter(Char::isDigit).take(9)) } },
                error = errors.price,
                keyboardType = KeyboardType.Number
            )
            FormField(
                label = "Laboratorio / Marca (opcional)",
                value = state.brand,
                onValueChange = { v -> viewModel.update { it.copy(brand = v) } },
                error = null
            )

            // ---------- Inventario ----------
            SectionTitle("Inventario")
            FormField(
                label = "Ubicación de almacenamiento",
                value = state.location,
                onValueChange = { v -> viewModel.update { it.copy(location = v) } },
                error = errors.location,
                placeholder = "Ej: Estante A-2"
            )
            DropdownField(
                label = "Unidad de medida",
                value = state.unit,
                options = ProductOptions.units,
                error = null,
                onSelected = { v -> viewModel.update { it.copy(unit = v) } }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField(
                    label = "Stock mínimo",
                    value = state.minStock,
                    onValueChange = { v -> viewModel.update { it.copy(minStock = v.filter(Char::isDigit).take(9)) } },
                    error = errors.minStock,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                // El stock inicial solo se pide al crear; después cambia con movimientos
                if (!state.isEdit) {
                    FormField(
                        label = "Cantidad inicial",
                        value = state.stock,
                        onValueChange = { v -> viewModel.update { it.copy(stock = v.filter(Char::isDigit).take(9)) } },
                        error = errors.stock,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ---------- Lote y vencimiento (solo al crear) ----------
            if (!state.isEdit) {
                SectionTitle("Lote y vencimiento")
                FormField(
                    label = "N° de lote (opcional)",
                    value = state.lotNumber,
                    onValueChange = { v -> viewModel.update { it.copy(lotNumber = v.take(20)) } },
                    error = null,
                    placeholder = "Ej: 2026A"
                )
                // Campo de solo lectura: al tocarlo se abre el calendario
                Box {
                    OutlinedTextField(
                        value = state.expiryMillis?.let { ExpiryUtils.format(it) }.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de vencimiento (mes/año)") },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        isError = errors.expiry != null,
                        supportingText = errors.expiry?.let { { Text(it) } },
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
            }

            // ---------- Información médica ----------
            SectionTitle("Información médica (opcional)")
            FormField(
                label = "Indicaciones",
                value = state.indications,
                onValueChange = { v -> viewModel.update { it.copy(indications = v) } },
                error = null,
                minLines = 2
            )
            FormField(
                label = "Dosis recomendada",
                value = state.dosage,
                onValueChange = { v -> viewModel.update { it.copy(dosage = v) } },
                error = null,
                minLines = 2
            )
            FormField(
                label = "Contraindicaciones",
                value = state.contraindications,
                onValueChange = { v -> viewModel.update { it.copy(contraindications = v) } },
                error = null,
                minLines = 2
            )

            // ---------- Botones ----------
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("CANCELAR", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = {
                        viewModel.save { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            onClose()
                        }
                    },
                    enabled = !state.saving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (state.isEdit) "GUARDAR CAMBIOS" else "CONFIRMAR Y AGREGAR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ---------- Calendario ----------
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.expiryMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.update { it.copy(expiryMillis = pickerState.selectedDateMillis) }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

// =====================================================================
//  Componentes reutilizables del formulario
// =====================================================================

@Composable
private fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = TealPrimary
)

/** Campo de texto con error: isError + supportingText (Guía 11). */
@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null,
    readOnly: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        singleLine = minLines == 1,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors(),
        modifier = modifier
    )
}

/** Menú desplegable simple: campo de solo lectura + DropdownMenu. */
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    error: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente que recibe el toque y abre el menú
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}