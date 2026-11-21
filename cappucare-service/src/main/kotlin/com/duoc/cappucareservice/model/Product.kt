package com.duoc.cappucareservice.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

/** Mismos campos (y nombres JSON) que ProductEntity de la app Android. */
@Entity
@Table(name = "products")
class Product(
    @Id
    @field:NotBlank(message = "El SKU es obligatorio")
    var sku: String = "",

    @field:NotBlank(message = "El nombre es obligatorio")
    var name: String = "",

    var category: String = "",

    @field:Min(0, message = "El stock no puede ser negativo")
    var stock: Int = 0,

    @field:Min(0, message = "El stock mínimo no puede ser negativo")
    var minStock: Int = 0,

    var location: String = "",
    var expiryDate: String = "",
    var barcode: String = "",

    @field:Min(0, message = "El precio no puede ser negativo")
    var price: Int = 0,

    var brand: String = "",
    var unit: String = "Unidades",

    @Column(length = 1000) var indications: String = "",
    @Column(length = 1000) var dosage: String = "",
    @Column(length = 1000) var contraindications: String = ""
)
