package com.duoc.cappucare

import com.duoc.cappucare.model.ExpiryStatus
import com.duoc.cappucare.model.ExpiryUtils
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ExpiryUtilsTest : DescribeSpec({

    describe("ExpiryUtils.status") {

        it("retorna EXPIRED para un mes pasado") {
            ExpiryUtils.status("01/2020") shouldBe ExpiryStatus.EXPIRED
        }

        it("retorna SOON para un producto que vence dentro de 30 días") {
            val hoy = java.time.LocalDate.now()
            val mesActual = "%02d/%04d".format(hoy.monthValue, hoy.year)
            val resultado = ExpiryUtils.status(mesActual)
            assert(resultado == ExpiryStatus.SOON || resultado == ExpiryStatus.EXPIRED)
        }

        it("retorna OK para una fecha lejana en el futuro") {
            ExpiryUtils.status("12/2099") shouldBe ExpiryStatus.OK
        }

        it("retorna UNKNOWN para un formato inválido") {
            ExpiryUtils.status("fecha-invalida") shouldBe ExpiryStatus.UNKNOWN
            ExpiryUtils.status("") shouldBe ExpiryStatus.UNKNOWN
            ExpiryUtils.status("2026-12") shouldBe ExpiryStatus.UNKNOWN
        }
    }

    describe("ExpiryUtils.format") {

        it("formatea correctamente milisegundos a MM/yyyy") {
            val millis = java.time.LocalDate.of(2027, 1, 1)
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
            ExpiryUtils.format(millis) shouldBe "01/2027"
        }

        it("formatea correctamente diciembre 2030") {
            val millis = java.time.LocalDate.of(2030, 12, 15)
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
            ExpiryUtils.format(millis) shouldBe "12/2030"
        }
    }
})