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
