package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        val sharedPrefs = getSharedPreferences("finance_prefs", MODE_PRIVATE)
        val lastCrashTrace = sharedPrefs.getString("last_crash_trace", null)

        // Set global Uncaught Exception Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val trace = android.util.Log.getStackTraceString(throwable)
            sharedPrefs.edit().putString("last_crash_trace", trace).commit()
            defaultHandler?.uncaughtException(thread, throwable)
        }

        if (lastCrashTrace != null) {
            setContent {
                MyApplicationTheme(darkTheme = true) {
                    Scaffold { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF121212))
                                .padding(innerPadding)
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Application Diagnostics",
                                color = Color(0xFFEF5350),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Finance Tracker recovered from an unexpected crash on the last session. The exception details are below:",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            
                            // Stacktrace container
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.medium)
                                    .padding(12.dp)
                            ) {
                                val scrollState = rememberScrollState()
                                Text(
                                    text = lastCrashTrace,
                                    color = Color(0xFFFFB74D),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                                    onClick = {
                                        // Clear all pref state & reset DB
                                        sharedPrefs.edit().clear().commit()
                                        try {
                                            deleteDatabase("finance_database")
                                        } catch (e: Exception) {}
                                        // Restart intent
                                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                                        if (intent != null) {
                                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                ) {
                                    Text("Reset Data & Restart", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                    onClick = {
                                        sharedPrefs.edit().remove("last_crash_trace").commit()
                                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                                        if (intent != null) {
                                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                ) {
                                    Text("Retry Launch", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            return
        }

        // Initialize SQLite Room database elements
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(database.financeDao())

        // ViewModel initialization using the Repository factory
        val viewModel: FinanceViewModel by viewModels {
            FinanceViewModelFactory(repository, applicationContext)
        }

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                FinanceAppScreen(viewModel = viewModel)
            }
        }
    }
}
