package com.duoc.cappucareservice

import com.duoc.cappucareservice.model.Product
import com.duoc.cappucareservice.repository.ProductRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/** Carga los mismos productos de ejemplo (ficticios) que usa la app. */
@Component
class DataSeeder(private val products: ProductRepository) : CommandLineRunner {
    override fun run(vararg args: String) {
        if (products.count() > 0) return
        products.saveAll(
            listOf(
                Product("CAP-MED-001", "Paracetamol 500 mg", "Fármacos", 5, 10, "Estante B-4", "12/2026",
                    "7801234500011", 3500, "Laboratorio Chile", "Cajas"),
                Product("CAP-GUA-004", "Guantes de Nitrilo (Talla M)", "Desechables", 4, 15, "Estante B-1", "03/2028",
                    "7801234500042", 8900, "MedGlove", "Cajas"),
                Product("CAP-MED-012", "Mascarilla Quirúrgica (KN95)", "Quirúrgicos", 150, 20, "Estante A-2", "08/2027",
                    "9781882102775", 12500, "ProtecMed", "Cajas"),
                Product("CAP-ALC-088", "Gel 70% (1 Litro)", "Desechables", 28, 10, "Estante C-3", "01/2027",
                    "7801234500888", 4200, "HigiPlus", "Botellas"),
                Product("CAP-JER-005", "Jeringa 5 ml", "Quirúrgicos", 8, 20, "Estante B-2", "06/2028",
                    "7801234500055", 150, "MedSafe", "Unidades")
            )
        )
    }
}
