package com.duoc.cappucare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    const val PROFILE = "profile"
    const val INVENTORY = "inventory"
    const val DETAIL = "detail/{sku}"
    fun detail(sku: String) = "detail/$sku"
}

@Composable
fun AppNavHost(viewModel: CappucareViewModel) {
    val navController = rememberNavController()

    val products by viewModel.products.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val movements by viewModel.movements.collectAsState()
    val message by viewModel.message.collectAsState()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

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
                onNavigateToScan = { },
                onNavigateToAdd = { },
                onNavigateToInventory = { navController.navigate(Routes.INVENTORY) }
            )
        }

        composable(Routes.INVENTORY) {
            InventoryScreen(
                products = products,
                onProductClick = { sku -> navController.navigate(Routes.detail(sku)) },
                onBack = { navController.popBackStack() }
            )
        }

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