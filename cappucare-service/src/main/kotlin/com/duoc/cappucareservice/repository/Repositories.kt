package com.duoc.cappucareservice.repository

import com.duoc.cappucareservice.model.Movement
import com.duoc.cappucareservice.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductRepository : JpaRepository<Product, String> {
    @Query("select p from Product p where p.stock <= p.minStock order by p.name")
    fun findLowStock(): List<Product>
}

interface MovementRepository : JpaRepository<Movement, Long> {
    fun findAllByOrderByDateDesc(): List<Movement>
    fun findByProductSkuOrderByDateDesc(productSku: String): List<Movement>
    fun deleteByProductSku(productSku: String)
}
