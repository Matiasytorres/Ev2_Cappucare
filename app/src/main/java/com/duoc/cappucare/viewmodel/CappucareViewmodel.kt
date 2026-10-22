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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CappucareViewModel(private val database: AppDatabase) : ViewModel() {

    private val dao = database.dao()

    init {
        viewModelScope.launch {
            if (dao.countProducts() == 0) {
                val samples = listOf(
                    ProductEntity("CAP-MED-001", "Paracetamol 500 mg", "Fármacos", 5, 10, "Estante B-4", "12/2026",
                        barcode = "7801234500011", price = 3500, brand = "Laboratorio Chile", unit = "Cajas"),
                    ProductEntity("CAP-GUA-004", "Guantes de Nitrilo (Talla M)", "Desechables", 4, 15, "Estante B-1", "03/2028",
                        barcode = "7801234500042", price = 8900, brand = "MedGlove", unit = "Cajas"),
                    ProductEntity("CAP-MED-012", "Mascarilla Quirúrgica (KN95)", "Quirúrgicos", 150, 20, "Estante A-2", "08/2027",
                        barcode = "9781882102775", price = 12500, brand = "ProtecMed", unit = "Cajas"),
                    ProductEntity("CAP-ALC-088", "Gel 70% (1 Litro)", "Desechables", 28, 10, "Estante C-3", "01/2027",
                        barcode = "7801234500888", price = 4200, brand = "HigiPlus", unit = "Botellas"),
                    ProductEntity("CAP-JER-005", "Jeringa 5 ml", "Quirúrgicos", 8, 20, "Estante B-2", "06/2028",
                        barcode = "7801234500055", price = 150, brand = "MedSafe", unit = "Unidades"),

                            ProductEntity("CAP-MED-001", "Paracetamol 500 mg", "Fármacos", 5, 10, "Estante B-4", "12/2026",
                    barcode = "7801234500011", price = 3500, brand = "Laboratorio Chile", unit = "Cajas",
                    indications = "Alivio del dolor leve a moderado y de la fiebre (dato ficticio de ejemplo).",
                    dosage = "Adultos: 1 comprimido cada 6 a 8 horas (dato ficticio de ejemplo).",
                    contraindications = "Hipersensibilidad al paracetamol. Insuficiencia hepática grave (dato ficticio de ejemplo)."),
                )
                samples.forEach { p ->
                    dao.insertProduct(p)
                    dao.insertLot(
                        LotEntity(
                            productSku = p.sku,
                            lotNumber = "2026A",
                            expiryDate = p.expiryDate,
                            quantity = p.stock,
                            location = p.location
                        )
                    )
                }
            }
        }
    }

    val products: StateFlow<List<ProductEntity>> = dao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = dao.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movements: StateFlow<List<MovementEntity>> = dao.getAllMovements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun lotsFor(sku: String): Flow<List<LotEntity>> = dao.getLotsByProduct(sku)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun findProductByCode(code: String, onResult: (ProductEntity?) -> Unit) {
        viewModelScope.launch {
            val product = dao.getProductByCode(code)
            onResult(product)
        }
    }

    fun addProduct(product: ProductEntity, lotNumber: String) {
        viewModelScope.launch {
            if (product.sku.isBlank() || product.name.isBlank()) {
                _message.value = "Código y nombre son obligatorios"
                return@launch
            }
            if (product.stock < 0 || product.minStock < 0 || product.price < 0) {
                _message.value = "Los valores no pueden ser negativos"
                return@launch
            }
            if (dao.getProductBySku(product.sku) != null) {
                _message.value = "Ya existe un producto con ese código"
                return@launch
            }
            database.withTransaction {
                dao.insertProduct(product)
                if (product.stock > 0) {
                    dao.insertLot(
                        LotEntity(
                            productSku = product.sku,
                            lotNumber = lotNumber.ifBlank { "S/L" },
                            expiryDate = product.expiryDate,
                            quantity = product.stock,
                            location = product.location
                        )
                    )
                }
            }
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