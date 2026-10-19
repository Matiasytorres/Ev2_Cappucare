package com.duoc.cappucare.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.duoc.cappucare.ui.screens.HomeScreen
import com.duoc.cappucare.ui.screens.InventoryScreen
import com.duoc.cappucare.ui.screens.LoginScreen
import com.duoc.cappucare.ui.screens.ProfileScreen
import com.duoc.cappucare.viewmodel.CappucareViewModel

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val INVENTORY = "inventory"
}

@Composable
fun AppNavHost(viewModel: CappucareViewModel) {
    val navController = rememberNavController()

    val products by viewModel.products.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val movements by viewModel.movements.collectAsState()

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
                onProductClick = { },
                onBack = { navController.popBackStack() }
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