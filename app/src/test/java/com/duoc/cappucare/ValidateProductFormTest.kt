package com.duoc.cappucare

import com.duoc.cappucare.model.ProductFormUiState
import com.duoc.cappucare.model.validateProductForm
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ValidateProductFormTest : DescribeSpec({

    val estadoValido = ProductFormUiState(
        sku = "CAP-TST-001",
        name = "Producto de prueba",
        category = "Fármacos",
        barcode = "7801234500011",
        price = "5000",
        location = "Estante A-1",
        minStock = "5",
        stock = "20",
        expiryMillis = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000 // +90 días
    )

    describe("SKU") {
        it("es válido cuando cumple el formato") {
            val errores = validateProductForm(estadoValido, skuTaken = false)
            errores.sku.shouldBeNull()
        }

        it("da error si está vacío") {
            val errores = validateProductForm(estadoValido.copy(sku = ""), skuTaken = false)
            errores.sku.shouldNotBeNull()
        }

        it("da error si es demasiado corto") {
            val errores = validateProductForm(estadoValido.copy(sku = "AB"), skuTaken = false)
            errores.sku.shouldNotBeNull()
        }

        it("da error si ya existe ese SKU") {
            val errores = validateProductForm(estadoValido, skuTaken = true)
            errores.sku.shouldNotBeNull()
            errores.sku shouldBe "Ya existe un producto con este SKU"
        }

        it("ignora la validación de SKU en modo edición") {
            val errores = validateProductForm(estadoValido.copy(isEdit = true, sku = ""), skuTaken = false)
            errores.sku.shouldBeNull()
        }
    }

    describe("Nombre") {
        it("es válido cuando tiene al menos 3 caracteres") {
            val errores = validateProductForm(estadoValido, skuTaken = false)
            errores.name.shouldBeNull()
        }

        it("da error si está vacío") {
            val errores = validateProductForm(estadoValido.copy(name = ""), skuTaken = false)
            errores.name.shouldNotBeNull()
        }

        it("da error si tiene menos de 3 caracteres") {
            val errores = validateProductForm(estadoValido.copy(name = "AB"), skuTaken = false)
            errores.name.shouldNotBeNull()
        }
    }

    describe("Categoría") {
        it("da error si está vacía") {
            val errores = validateProductForm(estadoValido.copy(category = ""), skuTaken = false)
            errores.category.shouldNotBeNull()
        }

        it("es válida cuando tiene valor") {
            val errores = validateProductForm(estadoValido, skuTaken = false)
            errores.category.shouldBeNull()
        }
    }

    describe("Precio") {
        it("da error si está vacío") {
            val errores = validateProductForm(estadoValido.copy(price = ""), skuTaken = false)
            errores.price.shouldNotBeNull()
        }

        it("da error si es cero") {
            val errores = validateProductForm(estadoValido.copy(price = "0"), skuTaken = false)
            errores.price.shouldNotBeNull()
        }

        it("da error si es negativo") {
            val errores = validateProductForm(estadoValido.copy(price = "-100"), skuTaken = false)
            errores.price.shouldNotBeNull()
        }

        it("es válido cuando es mayor a 0") {
            val errores = validateProductForm(estadoValido.copy(price = "1"), skuTaken = false)
            errores.price.shouldBeNull()
        }
    }

    describe("Fecha de vencimiento") {
        it("da error si es nula") {
            val errores = validateProductForm(estadoValido.copy(expiryMillis = null), skuTaken = false)
            errores.expiry.shouldNotBeNull()
        }

        it("da error si ya está vencida") {
            val pasado = System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000 // -10 días
            val errores = validateProductForm(estadoValido.copy(expiryMillis = pasado), skuTaken = false)
            errores.expiry.shouldNotBeNull()
        }

        it("es válida si es futura") {
            val errores = validateProductForm(estadoValido, skuTaken = false)
            errores.expiry.shouldBeNull()
        }

        it("ignora la validación de expiración en modo edición") {
            val errores = validateProductForm(estadoValido.copy(isEdit = true, expiryMillis = null), skuTaken = false)
            errores.expiry.shouldBeNull()
        }
    }

    describe("Formulario completo") {
        it("no tiene errores con un estado completamente válido") {
            val errores = validateProductForm(estadoValido, skuTaken = false)
            errores.hasErrors shouldBe false
        }

        it("detecta múltiples errores a la vez") {
            val estado = estadoValido.copy(sku = "", name = "", price = "")
            val errores = validateProductForm(estado, skuTaken = false)
            errores.hasErrors shouldBe true
            errores.sku.shouldNotBeNull()
            errores.name.shouldNotBeNull()
            errores.price.shouldNotBeNull()
        }
    }
})