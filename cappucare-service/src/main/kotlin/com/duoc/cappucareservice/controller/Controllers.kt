package com.duoc.cappucareservice.controller

import com.duoc.cappucareservice.model.Movement
import com.duoc.cappucareservice.model.MovementRequest
import com.duoc.cappucareservice.model.Product
import com.duoc.cappucareservice.service.InventoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductController(private val service: InventoryService) {

    @GetMapping
    fun list(): List<Product> = service.listProducts()

    @GetMapping("/low-stock")
    fun lowStock(): List<Product> = service.lowStock()

    @GetMapping("/{sku}")
    fun get(@PathVariable sku: String): Product = service.getProduct(sku)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody product: Product): Product = service.createProduct(product)

    @PutMapping("/{sku}")
    fun update(@PathVariable sku: String, @Valid @RequestBody product: Product): Product =
        service.updateProduct(sku, product)

    @DeleteMapping("/{sku}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable sku: String) = service.deleteProduct(sku)
}

@RestController
@RequestMapping("/api/movements")
class MovementController(private val service: InventoryService) {

    @GetMapping
    fun list(@RequestParam(required = false) sku: String?): List<Movement> = service.listMovements(sku)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: MovementRequest): Movement = service.registerMovement(request)
}
