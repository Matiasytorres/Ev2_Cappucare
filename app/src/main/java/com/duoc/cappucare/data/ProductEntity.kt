package com.duoc.cappucare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val sku: String,
    val name: String,
    val category: String,
    val stock: Int,
    val minStock: Int,
    val location: String,
    val expiryDate: String
)

@Entity(tableName = "movements")
data class MovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productSku: String,
    val productName: String,
    val type: String, // "INGRESO" o "VENTA"
    val quantity: Int,
    val timestamp: String,
    val location: String
)