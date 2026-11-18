package com.duoc.cappucare

import com.duoc.cappucare.data.AppDatabase
import com.duoc.cappucare.data.CappucareDao
import com.duoc.cappucare.data.MovementType
import com.duoc.cappucare.data.ProductEntity
import com.duoc.cappucare.viewmodel.CappucareViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("CappucareViewModel")
class CappucareViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val productoEjemplo = ProductEntity(
        sku        = "CAP-TST-001",
        name       = "Paracetamol Test",
        category   = "Fármacos",
        stock      = 20,
        minStock   = 5,
        location   = "Estante A-1",
        expiryDate = "12/2027",
        price      = 3500
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun crearViewModel(): Pair<CappucareViewModel, CappucareDao> {
        val dao = mockk<CappucareDao>(relaxed = true)
        val db  = mockk<AppDatabase>(relaxed = true)
        every { db.dao() }                  returns dao
        every { dao.getAllProducts() }      returns flowOf(listOf(productoEjemplo))
        every { dao.getLowStockProducts() } returns flowOf(emptyList())
        every { dao.getAllMovements() }     returns flowOf(emptyList())
        coEvery { dao.countProducts() }    returns 1
        return Pair(CappucareViewModel(db), dao)
    }


    @Test
    @DisplayName("registerMovement: da error cuando la cantidad es 0")
    fun registerMovement_cantidad0() = runTest(testDispatcher) {
        val (vm, _) = crearViewModel()
        advanceUntilIdle()

        vm.registerMovement("CAP-TST-001", MovementType.SALIDA, 0, "Test", "Tester")
        advanceUntilIdle()

        assertEquals("La cantidad debe ser mayor a 0", vm.message.value)
    }

    @Test
    @DisplayName("registerMovement: da error cuando la cantidad es negativa")
    fun registerMovement_cantidadNegativa() = runTest(testDispatcher) {
        val (vm, _) = crearViewModel()
        advanceUntilIdle()

        vm.registerMovement("CAP-TST-001", MovementType.SALIDA, -3, "Test", "Tester")
        advanceUntilIdle()

        assertEquals("La cantidad debe ser mayor a 0", vm.message.value)
    }


    @Test
    @DisplayName("addProduct: da error cuando el SKU está vacío")
    fun addProduct_skuVacio() = runTest(testDispatcher) {
        val (vm, _) = crearViewModel()
        advanceUntilIdle()

        vm.addProduct(productoEjemplo.copy(sku = ""), lotNumber = "2026A")
        advanceUntilIdle()

        assertEquals("Código y nombre son obligatorios", vm.message.value)
    }

    @Test
    @DisplayName("addProduct: da error cuando el nombre está vacío")
    fun addProduct_nombreVacio() = runTest(testDispatcher) {
        val (vm, _) = crearViewModel()
        advanceUntilIdle()

        vm.addProduct(productoEjemplo.copy(name = ""), lotNumber = "2026A")
        advanceUntilIdle()

        assertEquals("Código y nombre son obligatorios", vm.message.value)
    }

    @Test
    @DisplayName("addProduct: da error cuando el stock es negativo")
    fun addProduct_stockNegativo() = runTest(testDispatcher) {
        val (vm, _) = crearViewModel()
        advanceUntilIdle()

        vm.addProduct(productoEjemplo.copy(stock = -1), lotNumber = "2026A")
        advanceUntilIdle()

        assertEquals("Los valores no pueden ser negativos", vm.message.value)
    }

    @Test
    @DisplayName("addProduct: da error cuando el precio es negativo")
    fun addProduct_precioNegativo() = runTest(testDispatcher) {
        val (vm, _) = crearViewModel()
        advanceUntilIdle()

        vm.addProduct(productoEjemplo.copy(price = -100), lotNumber = "2026A")
        advanceUntilIdle()

        assertEquals("Los valores no pueden ser negativos", vm.message.value)
    }

    @Test
    @DisplayName("addProduct: da error cuando el minStock es negativo")
    fun addProduct_minStockNegativo() = runTest(testDispatcher) {
        val (vm, _) = crearViewModel()
        advanceUntilIdle()

        vm.addProduct(productoEjemplo.copy(minStock = -1), lotNumber = "2026A")
        advanceUntilIdle()

        assertEquals("Los valores no pueden ser negativos", vm.message.value)
    }

    @Test
    @DisplayName("addProduct: da error cuando el SKU ya existe en la BD")
    fun addProduct_skuDuplicado() = runTest(testDispatcher) {
        val (vm, dao) = crearViewModel()
        coEvery { dao.getProductBySku("CAP-TST-001") } returns productoEjemplo
        advanceUntilIdle()

        vm.addProduct(productoEjemplo, lotNumber = "2026A")
        advanceUntilIdle()

        assertEquals("Ya existe un producto con ese código", vm.message.value)
    }


    @Test
    @DisplayName("clearMessage: pone el mensaje en null")
    fun clearMessage_limpia() = runTest(testDispatcher) {
        val (vm, _) = crearViewModel()
        advanceUntilIdle()

        vm.addProduct(productoEjemplo.copy(sku = ""), lotNumber = "")
        advanceUntilIdle()
        assertNotNull(vm.message.value)

        vm.clearMessage()
        assertNull(vm.message.value)
    }


    @Test
    @DisplayName("findProductByCode: retorna el producto cuando el código existe")
    fun findProductByCode_existe() = runTest(testDispatcher) {
        val (vm, dao) = crearViewModel()
        coEvery { dao.getProductByCode("7801234500011") } returns productoEjemplo
        advanceUntilIdle()

        var resultado: ProductEntity? = null
        vm.findProductByCode("7801234500011") { resultado = it }
        advanceUntilIdle()

        assertEquals(productoEjemplo, resultado)
    }

    @Test
    @DisplayName("findProductByCode: retorna null cuando el código no existe")
    fun findProductByCode_noExiste() = runTest(testDispatcher) {
        val (vm, dao) = crearViewModel()
        coEvery { dao.getProductByCode("CODIGO-FALSO") } returns null
        advanceUntilIdle()

        var resultado: ProductEntity? = productoEjemplo
        vm.findProductByCode("CODIGO-FALSO") { resultado = it }
        advanceUntilIdle()

        assertNull(resultado)
    }
}