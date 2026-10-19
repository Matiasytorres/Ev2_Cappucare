package com.duoc.cappucare.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MovementType { INGRESO, SALIDA }

@Entity(
    tableName = "movements",
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
data class MovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productSku: String,
    val date: Long = System.currentTimeMillis(),
    val type: MovementType,
    val quantity: Int,
    val reason: String = "",
    val responsible: String = ""
)