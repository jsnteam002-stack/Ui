package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainAppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EsportsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge immersive display for modern full-bleed mobile views
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val viewModel: EsportsViewModel = viewModel()
                MainAppNavigation(viewModel = viewModel)
            }
        }
    }
}
