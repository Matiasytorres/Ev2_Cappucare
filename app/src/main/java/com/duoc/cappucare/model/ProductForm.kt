package com.duoc.cappucare.model

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Opciones fijas de los menús desplegables del formulario. */
object ProductOptions {
    val categories = listOf("Fármacos", "Quirúrgicos", "Desechables", "Higiene y desinfección", "Equipamiento")
    val units = listOf("Unidades", "Cajas", "Botellas", "Frascos", "Paquetes")
}

/**
 * Estado del formulario Agregar/Editar producto.
 * Los campos numéricos se guardan como String porque es lo que escribe el usuario;
 * se convierten a Int recién al guardar (después de validar).
 */
data class ProductFormUiState(
    val sku: String = "",
    val name: String = "",
    val category: String = "",
    val barcode: String = "",
    val price: String = "",
    val brand: String = "",
    val location: String = "",
    val unit: String = "Unidades",
    val stock: String = "",
    val minStock: String = "",
    val lotNumber: String = "",
    val expiryMillis: Long? = null,
    val indications: String = "",
    val dosage: String = "",
    val contraindications: String = "",
    val imageUri: String? = null,
    val isEdit: Boolean = false,
    val loading: Boolean = false,
    val saving: Boolean = false
)

/** Un mensaje de error por campo (null = campo válido). */
data class ProductFormErrors(
    val sku: String? = null,
    val name: String? = null,
    val category: String? = null,
    val barcode: String? = null,
    val price: String? = null,
    val location: String? = null,
    val minStock: String? = null,
    val stock: String? = null,
    val expiry: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(sku, name, category, barcode, price, location, minStock, stock, expiry)
            .any { it != null }
}

private const val ONE_DAY_MILLIS = 86_400_000L
private val SKU_REGEX = Regex("^[A-Za-z0-9-]{3,20}$")
private val BARCODE_REGEX = Regex("^[A-Za-z0-9]{6,20}$")

/** Reglas de validación del formulario. Función pura: fácil de probar con tests unitarios. */
fun validateProductForm(s: ProductFormUiState, skuTaken: Boolean): ProductFormErrors {
    val expiry = s.expiryMillis
    return ProductFormErrors(
        sku = when {
            s.isEdit -> null
            s.sku.isBlank() -> "El código SKU es obligatorio"
            !SKU_REGEX.matches(s.sku.trim()) -> "Usa de 3 a 20 letras, números o guiones"
            skuTaken -> "Ya existe un producto con este SKU"
            else -> null
        },
        name = when {
            s.name.isBlank() -> "El nombre es obligatorio"
            s.name.trim().length < 3 -> "Escribe al menos 3 caracteres"
            else -> null
        },
        category = if (s.category.isBlank()) "Selecciona una categoría" else null,
        barcode = if (s.barcode.isNotBlank() && !BARCODE_REGEX.matches(s.barcode.trim())) {
            "De 6 a 20 caracteres, sin espacios"
        } else null,
        price = when {
            s.price.isBlank() -> "El precio es obligatorio"
            (s.price.toIntOrNull() ?: 0) <= 0 -> "Debe ser mayor a 0"
            else -> null
        },
        location = if (s.location.isBlank()) "La ubicación es obligatoria" else null,
        minStock = if (s.minStock.isBlank()) "Obligatorio" else null,
        stock = if (!s.isEdit && s.stock.isBlank()) "Ingresa la cantidad inicial" else null,
        expiry = when {
            s.isEdit -> null
            expiry == null -> "Selecciona la fecha de vencimiento"
            expiry < System.currentTimeMillis() - ONE_DAY_MILLIS -> "El producto ya está vencido"
            else -> null
        }
    )
}

enum class ExpiryStatus { OK, SOON, EXPIRED, UNKNOWN }

/**
 * Utilidades de vencimiento. Hoy la fecha se guarda como texto "MM/yyyy".
 * Cuando la tarjeta de ProductEntity migre a fecha real, solo hay que cambiar este objeto.
 */
object ExpiryUtils {
    private val monthYear = DateTimeFormatter.ofPattern("MM/yyyy")

    /** Convierte los milisegundos del DatePicker (siempre en UTC) a "MM/yyyy". */
    fun format(millis: Long): String {
        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
        return YearMonth.from(date).format(monthYear)
    }

    /** Vencido, por vencer (dentro de [soonDays] días) o vigente. El producto vale hasta fin de mes. */
    fun status(expiry: String, soonDays: Long = 60): ExpiryStatus {
        val month = runCatching { YearMonth.parse(expiry, monthYear) }.getOrNull()
            ?: return ExpiryStatus.UNKNOWN
        val lastDay = month.atEndOfMonth()
        val today = LocalDate.now()
        return when {
            lastDay.isBefore(today) -> ExpiryStatus.EXPIRED
            lastDay.isBefore(today.plusDays(soonDays)) -> ExpiryStatus.SOON
            else -> ExpiryStatus.OK
        }
    }
}