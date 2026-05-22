package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.ui.screens.FinanceAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room database elements
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(database.financeDao())

        // ViewModel initialization using the Repository factory
        val viewModel: FinanceViewModel by viewModels {
            FinanceViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                FinanceAppScreen(viewModel = viewModel)
            }
        }
    }
}
