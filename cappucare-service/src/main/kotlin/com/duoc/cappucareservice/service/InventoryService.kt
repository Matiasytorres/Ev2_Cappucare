package com.duoc.cappucareservice.service

import com.duoc.cappucareservice.model.Movement
import com.duoc.cappucareservice.model.MovementRequest
import com.duoc.cappucareservice.model.MovementType
import com.duoc.cappucareservice.model.Product
import com.duoc.cappucareservice.repository.MovementRepository
import com.duoc.cappucareservice.repository.ProductRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class InventoryService(
    private val products: ProductRepository,
    private val movements: MovementRepository
) {

    fun listProducts(): List<Product> = products.findAll().sortedBy { it.name }

    fun lowStock(): List<Product> = products.findLowStock()

    fun getProduct(sku: String): Product =
        products.findById(sku).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Producto $sku no encontrado")
        }

    @Transactional
    fun createProduct(product: Product): Product {
        if (products.existsById(product.sku)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con SKU ${product.sku}")
        }
        return products.save(product)
    }

    @Transactional
    fun updateProduct(sku: String, data: Product): Product {
        getProduct(sku)
        data.sku = sku
        return products.save(data)
    }

    @Transactional
    fun deleteProduct(sku: String) {
        getProduct(sku)
        movements.deleteByProductSku(sku)
        products.deleteById(sku)
    }

    fun listMovements(sku: String?): List<Movement> =
        if (sku.isNullOrBlank()) movements.findAllByOrderByDateDesc()
        else movements.findByProductSkuOrderByDateDesc(sku)

    /** Ingreso/salida con la misma regla de la app: la salida no puede dejar stock negativo. */
    @Transactional
    fun registerMovement(req: MovementRequest): Movement {
        val product = getProduct(req.productSku)
        val type = req.type!!
        val newStock = when (type) {
            MovementType.INGRESO -> product.stock + req.quantity
            MovementType.SALIDA -> product.stock - req.quantity
        }
        if (newStock < 0) {
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Stock insuficiente: hay ${product.stock} unidades"
            )
        }
        product.stock = newStock
        products.save(product)
        return movements.save(
            Movement(
                productSku = product.sku,
                type = type,
                quantity = req.quantity,
                reason = req.reason,
                responsible = req.responsible
            )
        )
    }
}
