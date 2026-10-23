package com.duoc.cappucare.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.cappucare.data.AppDatabase
import com.duoc.cappucare.ui.screens.HistoryScreen
import com.duoc.cappucare.ui.screens.HomeScreen
import com.duoc.cappucare.ui.screens.InventoryScreen
import com.duoc.cappucare.ui.screens.LoginScreen
import com.duoc.cappucare.ui.screens.ProductDetailScreen
import com.duoc.cappucare.ui.screens.ProductFormScreen
import com.duoc.cappucare.ui.screens.ProfileScreen
import com.duoc.cappucare.ui.screens.ScannerScreen
import com.duoc.cappucare.viewmodel.CappucareViewModel
import com.duoc.cappucare.viewmodel.ProductFormViewModel
import com.duoc.cappucare.viewmodel.ProductFormViewModelFactory


object Routes {
    const val LOGIN      = "login"
    const val HOME       = "home"
    const val INVENTORY  = "inventory"
    const val SCAN       = "scan"
    const val HISTORY    = "history"
    const val PROFILE    = "profile"
    const val DETAIL     = "detail/{sku}"
    const val PRODUCT_FORM = "product_form?sku={sku}&barcode={barcode}"

    fun detail(sku: String) = "detail/$sku"

    fun productForm(sku: String? = null, barcode: String? = null): String {
        val params = buildList {
            sku?.let     { add("sku=${android.net.Uri.encode(it)}") }
            barcode?.let { add("barcode=${android.net.Uri.encode(it)}") }
        }
        return if (params.isEmpty()) "product_form" else "product_form?" + params.joinToString("&")
    }
}

@Composable
fun AppNavHost(viewModel: CappucareViewModel) {
    val navController = rememberNavController()
    val context       = LocalContext.current

    val database      = remember { AppDatabase.getDatabase(context) }


    val products  by viewModel.products.collectAsState()
    val lowStock  by viewModel.lowStockProducts.collectAsState()
    val movements by viewModel.movements.collectAsState()
    val message   by viewModel.message.collectAsState()


    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route


    val selectedRoute = when (currentRoute) {
        Routes.DETAIL, Routes.PRODUCT_FORM -> Routes.INVENTORY
        else -> currentRoute
    }


    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {

            if (currentRoute != null && currentRoute != Routes.LOGIN) {
                AppBottomBar(
                    selectedRoute = selectedRoute,
                    onNavigate    = { navigateTab(it) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.LOGIN,
            modifier         = Modifier.padding(innerPadding)
        ) {

            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    products            = products,
                    lowStockList        = lowStock,
                    movements           = movements,
                    onNavigateToScan    = { navigateTab(Routes.SCAN) },
                    onNavigateToAdd     = { navController.navigate(Routes.productForm()) },
                    onNavigateToInventory = { navigateTab(Routes.INVENTORY) }
                )
            }

            composable(Routes.INVENTORY) {
                InventoryScreen(
                    products       = products,
                    onProductClick = { sku -> navController.navigate(Routes.detail(sku)) },
                    onAddProduct   = { navController.navigate(Routes.productForm()) },
                    onRegisterSale = { sku -> navController.navigate(Routes.detail(sku)) },
                    onRequestRestock = { sku -> navController.navigate(Routes.detail(sku)) }
                )
            }

            composable(Routes.SCAN) {
                ScannerScreen(
                    onLookup = { code, onResult ->
                        viewModel.findProductByCode(code) { product -> onResult(product) }
                    },
                    onProductFound = { sku ->
                        navController.navigate(Routes.detail(sku))
                    },
                    onAddProduct = { code ->
                        navController.navigate(Routes.productForm(barcode = code))
                    }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen(
                    products        = products,
                    movements       = movements,
                    onMovementClick = { sku -> navController.navigate(Routes.detail(sku)) }
                )
            }

            composable(
                route     = Routes.DETAIL,
                arguments = listOf(navArgument("sku") { type = NavType.StringType })
            ) { entry ->
                val sku = entry.arguments?.getString("sku").orEmpty()

                val lotsFlow = remember(sku) { viewModel.lotsFor(sku) }
                val lots     by lotsFlow.collectAsState(initial = emptyList())

                ProductDetailScreen(
                    product    = products.find { it.sku == sku },
                    lots       = lots,
                    movements  = movements.filter { it.productSku == sku },
                    message    = message,
                    onRegister = { type, qty, reason, responsible ->
                        viewModel.registerMovement(sku, type, qty, reason, responsible)
                    },
                    onClearMessage = { viewModel.clearMessage() },
                    onEdit   = { navController.navigate(Routes.productForm(sku = sku)) },
                    onDelete = {
                        products.find { it.sku == sku }?.let { viewModel.deleteProduct(it) }
                        viewModel.clearMessage()
                        navController.popBackStack()
                    },
                    onBack = {
                        viewModel.clearMessage()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route     = Routes.PRODUCT_FORM,
                arguments = listOf(
                    navArgument("sku") {
                        type         = NavType.StringType
                        nullable     = true
                        defaultValue = null
                    },
                    navArgument("barcode") {
                        type         = NavType.StringType
                        nullable     = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                val sku     = entry.arguments?.getString("sku")
                val barcode = entry.arguments?.getString("barcode")

                val formViewModel: ProductFormViewModel =
                    viewModel(factory = ProductFormViewModelFactory(database))

                LaunchedEffect(sku, barcode) {
                    formViewModel.start(sku, barcode)
                }

                ProductFormScreen(
                    viewModel = formViewModel,
                    onClose   = { navController.popBackStack() }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    products      = products,
                    movements     = movements,
                    lowStockCount = lowStock.size,
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}