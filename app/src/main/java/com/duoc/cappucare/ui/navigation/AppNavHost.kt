package com.duoc.cappucare.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.cappucare.ui.screens.HomeScreen
import com.duoc.cappucare.ui.screens.InventoryScreen
import com.duoc.cappucare.ui.screens.LoginScreen
import com.duoc.cappucare.ui.screens.ProductDetailScreen
import com.duoc.cappucare.ui.screens.ProfileScreen
import com.duoc.cappucare.viewmodel.CappucareViewModel

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val INVENTORY = "inventory"
    const val SCAN = "scan"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{sku}"
    fun detail(sku: String) = "detail/$sku"
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$title (próximamente)")
    }
}

@Composable
fun AppNavHost(viewModel: CappucareViewModel) {
    val navController = rememberNavController()

    val products by viewModel.products.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val movements by viewModel.movements.collectAsState()
    val message by viewModel.message.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // El detalle de producto cuenta como parte de Inventario
    val selectedRoute = if (currentRoute == Routes.DETAIL) Routes.INVENTORY else currentRoute

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute != null && currentRoute != Routes.LOGIN) {
                AppBottomBar(selectedRoute = selectedRoute, onNavigate = { navigateTab(it) })
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
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
                    products = products,
                    lowStockList = lowStock,
                    movements = movements,
                    onNavigateToScan = { navigateTab(Routes.SCAN) },
                    onNavigateToAdd = { },
                    onNavigateToInventory = { navigateTab(Routes.INVENTORY) }
                )
            }

            composable(Routes.INVENTORY) {
                InventoryScreen(
                    products = products,
                    onProductClick = { sku -> navController.navigate(Routes.detail(sku)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SCAN) { PlaceholderScreen("Escanear código") }
            composable(Routes.HISTORY) { PlaceholderScreen("Historial") }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("sku") { type = NavType.StringType })
            ) { entry ->
                val sku = entry.arguments?.getString("sku").orEmpty()
                ProductDetailScreen(
                    product = products.find { it.sku == sku },
                    movements = movements.filter { it.productSku == sku },
                    message = message,
                    onRegister = { type, qty, reason, responsible ->
                        viewModel.registerMovement(sku, type, qty, reason, responsible)
                    },
                    onClearMessage = { viewModel.clearMessage() },
                    onBack = {
                        viewModel.clearMessage()
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
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