package com.duoc.cappucareservice.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

enum class MovementType { INGRESO, SALIDA }

@Entity
@Table(name = "movements")
class Movement(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var productSku: String = "",
    var date: Long = System.currentTimeMillis(),
    @Enumerated(EnumType.STRING)
    var type: MovementType = MovementType.INGRESO,
    var quantity: Int = 0,
    var reason: String = "",
    var responsible: String = ""
)

/** Cuerpo del POST /api/movements */
data class MovementRequest(
    @field:NotBlank(message = "productSku es obligatorio")
    val productSku: String = "",
    @field:NotNull(message = "type es obligatorio (INGRESO o SALIDA)")
    val type: MovementType? = null,
    @field:Min(1, message = "La cantidad debe ser mayor a 0")
    val quantity: Int = 0,
    val reason: String = "",
    val responsible: String = ""
)
