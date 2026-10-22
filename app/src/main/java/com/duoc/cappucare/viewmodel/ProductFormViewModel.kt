package com.duoc.cappucare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.duoc.cappucare.data.AppDatabase
import com.duoc.cappucare.data.LotEntity
import com.duoc.cappucare.data.MovementEntity
import com.duoc.cappucare.data.MovementType
import com.duoc.cappucare.data.ProductEntity
import com.duoc.cappucare.model.ExpiryUtils
import com.duoc.cappucare.model.ProductFormErrors
import com.duoc.cappucare.model.ProductFormUiState
import com.duoc.cappucare.model.validateProductForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del formulario Agregar/Editar producto (Guía 11).
 *  - state:  lo que el usuario ha escrito (MutableStateFlow)
 *  - errors: se calcula solo; empieza a mostrarse después del primer intento de guardar
 *            y luego se actualiza en vivo mientras el usuario corrige
 */
class ProductFormViewModel(private val database: AppDatabase) : ViewModel() {

    private val dao = database.dao()

    private val _state = MutableStateFlow(ProductFormUiState())
    val state: StateFlow<ProductFormUiState> = _state.asStateFlow()

    private val submitted = MutableStateFlow(false)
    private val skuTaken = MutableStateFlow(false)

    val errors: StateFlow<ProductFormErrors> =
        combine(_state, submitted, skuTaken) { s, wasSubmitted, taken ->
            if (wasSubmitted) validateProductForm(s, taken) else ProductFormErrors()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductFormErrors())

    private var started = false
    private var original: ProductEntity? = null

    /**
     * Se llama una vez al abrir la pantalla.
     *  - sku != null      -> modo edición (carga el producto)
     *  - barcode != null  -> modo nuevo con el código ya escrito (viene del escáner)
     */
    fun start(sku: String?, barcode: String?) {
        if (started) return
        started = true

        if (sku == null) {
            _state.update { it.copy(barcode = barcode.orEmpty()) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isEdit = true, loading = true) }
            val product = dao.getProductBySku(sku)
            if (product == null) {
                _state.update { it.copy(loading = false) }
                return@launch
            }
            original = product
            _state.value = ProductFormUiState(
                sku = product.sku,
                name = product.name,
                category = product.category,
                barcode = product.barcode,
                price = product.price.toString(),
                brand = product.brand,
                location = product.location,
                unit = product.unit,
                stock = product.stock.toString(),
                minStock = product.minStock.toString(),
                indications = product.indications,
                dosage = product.dosage,
                contraindications = product.contraindications,
                imageUri = product.imageUri,
                isEdit = true
            )
        }
    }

    /** Modifica uno o varios campos: viewModel.update { it.copy(name = "...") } */
    fun update(transform: (ProductFormUiState) -> ProductFormUiState) {
        val old = _state.value
        val new = transform(old)
        _state.value = new
        if (new.sku != old.sku) skuTaken.value = false
    }

    fun save(onSuccess: (message: String) -> Unit) {
        submitted.value = true
        val s = _state.value
        if (s.saving || validateProductForm(s, skuTaken.value).hasErrors) return

        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            try {
                if (s.isEdit) {
                    val orig = original ?: return@launch
                    // Editar NO cambia stock ni vencimiento: eso se hace con movimientos y lotes
                    dao.updateProduct(
                        orig.copy(
                            name = s.name.trim(),
                            category = s.category,
                            barcode = s.barcode.trim(),
                            price = s.price.toInt(),
                            brand = s.brand.trim(),
                            location = s.location.trim(),
                            unit = s.unit,
                            minStock = s.minStock.toInt(),
                            indications = s.indications.trim(),
                            dosage = s.dosage.trim(),
                            contraindications = s.contraindications.trim(),
                            imageUri = s.imageUri
                        )
                    )
                    onSuccess("Producto actualizado")
                } else {
                    val sku = s.sku.trim()
                    if (dao.getProductBySku(sku) != null) {
                        skuTaken.value = true
                        return@launch
                    }
                    val stock = s.stock.toInt()
                    val expiry = ExpiryUtils.format(s.expiryMillis!!)
                    val product = ProductEntity(
                        sku = sku,
                        name = s.name.trim(),
                        category = s.category,
                        stock = stock,
                        minStock = s.minStock.toInt(),
                        location = s.location.trim(),
                        expiryDate = expiry,
                        barcode = s.barcode.trim(),
                        price = s.price.toInt(),
                        brand = s.brand.trim(),
                        unit = s.unit,
                        imageUri = s.imageUri,
                        indications = s.indications.trim(),
                        dosage = s.dosage.trim(),
                        contraindications = s.contraindications.trim()
                    )
                    // Producto + lote + movimiento inicial: todo o nada
                    database.withTransaction {
                        dao.insertProduct(product)
                        if (stock > 0) {
                            dao.insertLot(
                                LotEntity(
                                    productSku = sku,
                                    lotNumber = s.lotNumber.trim().ifBlank { "S/L" },
                                    expiryDate = expiry,
                                    quantity = stock,
                                    location = product.location
                                )
                            )
                            dao.insertMovement(
                                MovementEntity(
                                    productSku = sku,
                                    type = MovementType.INGRESO,
                                    quantity = stock,
                                    reason = "Registro inicial"
                                )
                            )
                        }
                    }
                    onSuccess("Producto registrado")
                }
            } finally {
                _state.update { it.copy(saving = false) }
            }
        }
    }
}

class ProductFormViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductFormViewModel::class.java)) {
            return ProductFormViewModel(database) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}