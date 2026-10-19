package com.duoc.cappucare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.duoc.cappucare.data.AppDatabase
import com.duoc.cappucare.data.MovementEntity
import com.duoc.cappucare.data.MovementType
import com.duoc.cappucare.data.ProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CappucareViewModel(private val database: AppDatabase) : ViewModel() {

    private val dao = database.dao()

    val products: StateFlow<List<ProductEntity>> = dao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = dao.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movements: StateFlow<List<MovementEntity>> = dao.getAllMovements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mensaje para mostrar al usuario (éxito o error de validación)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.sku.isBlank() || product.name.isBlank()) {
                _message.value = "SKU y nombre son obligatorios"
                return@launch
            }
            if (product.stock < 0 || product.minStock < 0) {
                _message.value = "El stock no puede ser negativo"
                return@launch
            }
            if (dao.getProductBySku(product.sku) != null) {
                _message.value = "Ya existe un producto con ese SKU"
                return@launch
            }
            dao.insertProduct(product)
            _message.value = "Producto registrado"
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch { dao.deleteProduct(product) }
    }

    fun registerMovement(
        sku: String,
        type: MovementType,
        quantity: Int,
        reason: String,
        responsible: String
    ) {
        viewModelScope.launch {
            if (quantity <= 0) {
                _message.value = "La cantidad debe ser mayor a 0"
                return@launch
            }

            // Todo ocurre en una transacción: o se guarda el movimiento y el stock, o nada
            val error: String? = database.withTransaction {
                val product = dao.getProductBySku(sku)
                    ?: return@withTransaction "Producto no encontrado"

                val newStock = when (type) {
                    MovementType.INGRESO -> product.stock + quantity
                    MovementType.SALIDA -> product.stock - quantity
                }
                if (newStock < 0) {
                    return@withTransaction "Stock insuficiente: hay ${product.stock} unidades"
                }

                dao.insertMovement(
                    MovementEntity(
                        productSku = sku,
                        type = type,
                        quantity = quantity,
                        reason = reason,
                        responsible = responsible
                    )
                )
                dao.updateStock(sku, newStock)
                null
            }

            _message.value = error ?: "Movimiento registrado"
        }
    }
}

class CappucareViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CappucareViewModel::class.java)) {
            return CappucareViewModel(database) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}