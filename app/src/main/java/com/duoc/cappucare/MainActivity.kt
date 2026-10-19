package com.duoc.cappucare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.duoc.cappucare.data.AppDatabase
import com.duoc.cappucare.ui.screens.HomeScreen
import com.duoc.cappucare.ui.theme.CappucareTheme
import com.duoc.cappucare.viewmodel.CappucareViewModel
import com.duoc.cappucare.viewmodel.CappucareViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val viewModel: CappucareViewModel by viewModels {
        CappucareViewModelFactory(database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CappucareTheme {
                val products by viewModel.products.collectAsState()
                val lowStock by viewModel.lowStockProducts.collectAsState()
                val movements by viewModel.movements.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        HomeScreen(
                            products = products,
                            lowStockList = lowStock,
                            movements = movements,
                            onNavigateToScan = { },
                            onNavigateToAdd = { },
                            onNavigateToInventory = { }
                        )
                    }
                }
            }
        }
    }
}