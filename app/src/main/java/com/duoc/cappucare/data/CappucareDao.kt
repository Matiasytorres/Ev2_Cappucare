package com.duoc.cappucare.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CappucareDao {

    // ---------- Productos ----------
    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sku = :sku")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products WHERE stock <= minStock ORDER BY name")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int

    @Query("SELECT * FROM products WHERE barcode = :barcode OR sku = :barcode LIMIT 1")
    suspend fun getProductByCode(barcode: String): ProductEntity?

    @Query("SELECT * FROM lots WHERE productSku = :sku ORDER BY expiryDate")
    fun getLotsByProduct(sku: String): Flow<List<LotEntity>>

    @Insert
    suspend fun insertLot(lot: LotEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity): Int

    @Delete
    suspend fun deleteProduct(product: ProductEntity): Int

    @Query("UPDATE products SET stock = :newStock WHERE sku = :sku")
    suspend fun updateStock(sku: String, newStock: Int): Int

    // ---------- Movimientos ----------
    @Query("SELECT * FROM movements ORDER BY date DESC")
    fun getAllMovements(): Flow<List<MovementEntity>>

    @Query("SELECT * FROM movements WHERE productSku = :sku ORDER BY date DESC")
    fun getMovementsByProduct(sku: String): Flow<List<MovementEntity>>

    @Insert
    suspend fun insertMovement(movement: MovementEntity): Long

    @Update
    suspend fun updateMovement(movement: MovementEntity): Int

    @Delete
    suspend fun deleteMovement(movement: MovementEntity): Int
}