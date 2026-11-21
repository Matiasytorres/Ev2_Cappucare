package com.duoc.cappucare.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Contrato del microservicio Spring Boot (cappucare-service). */
interface CappucareApi {

    @GET("api/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/products/low-stock")
    suspend fun getLowStock(): List<ProductDto>

    @GET("api/products/{sku}")
    suspend fun getProduct(@Path("sku") sku: String): ProductDto

    @POST("api/products")
    suspend fun createProduct(@Body product: ProductDto): ProductDto

    @PUT("api/products/{sku}")
    suspend fun updateProduct(@Path("sku") sku: String, @Body product: ProductDto): ProductDto

    @DELETE("api/products/{sku}")
    suspend fun deleteProduct(@Path("sku") sku: String): Response<Unit>

    @GET("api/movements")
    suspend fun getMovements(@Query("sku") sku: String? = null): List<MovementDto>

    @POST("api/movements")
    suspend fun registerMovement(@Body movement: MovementRequestDto): MovementDto
}
