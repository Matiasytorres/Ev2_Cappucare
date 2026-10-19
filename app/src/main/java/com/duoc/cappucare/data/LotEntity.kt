package com.duoc.cappucare.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lots",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["sku"],
            childColumns = ["productSku"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productSku")]
)
data class LotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productSku: String,
    val lotNumber: String,
    val expiryDate: String,
    val quantity: Int,
    val location: String
)

