package com.duoc.cappucare.data.remote

import com.duoc.cappucare.data.ProductEntity
import com.google.gson.annotations.SerializedName

/** DTO del producto: mismos nombres JSON que el microservicio Spring Boot. */
data class ProductDto(
    @SerializedName("sku") val sku: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String = "",
    @SerializedName("stock") val stock: Int = 0,
    @SerializedName("minStock") val minStock: Int = 0,
    @SerializedName("location") val location: String = "",
    @SerializedName("expiryDate") val expiryDate: String = "",
    @SerializedName("barcode") val barcode: String = "",
    @SerializedName("price") val price: Int = 0,
    @SerializedName("brand") val brand: String = "",
    @SerializedName("unit") val unit: String = "Unidades",
    @SerializedName("indications") val indications: String = "",
    @SerializedName("dosage") val dosage: String = "",
    @SerializedName("contraindications") val contraindications: String = ""
)

data class MovementRequestDto(
    @SerializedName("productSku") val productSku: String,
    @SerializedName("type") val type: String, // "INGRESO" o "SALIDA"
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("reason") val reason: String = "",
    @SerializedName("responsible") val responsible: String = ""
)

data class MovementDto(
    @SerializedName("id") val id: Long,
    @SerializedName("productSku") val productSku: String,
    @SerializedName("date") val date: Long,
    @SerializedName("type") val type: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("reason") val reason: String = "",
    @SerializedName("responsible") val responsible: String = ""
)

fun ProductDto.toEntity(localImageUri: String? = null) = ProductEntity(
    sku = sku, name = name, category = category, stock = stock, minStock = minStock,
    location = location, expiryDate = expiryDate, barcode = barcode, price = price,
    brand = brand, unit = unit, imageUri = localImageUri,
    indications = indications, dosage = dosage, contraindications = contraindications
)

fun ProductEntity.toDto() = ProductDto(
    sku = sku, name = name, category = category, stock = stock, minStock = minStock,
    location = location, expiryDate = expiryDate, barcode = barcode, price = price,
    brand = brand, unit = unit,
    indications = indications, dosage = dosage, contraindications = contraindications
)
