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
import androidx.compose.ui.Modifier
import com.duoc.cappucare.data.AppDatabase
import com.duoc.cappucare.ui.navigation.AppNavHost
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavHost(viewModel)
                    }
                }
            }
        }
    }
}