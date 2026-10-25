package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainContainerScreen
import com.example.ui.theme.KerupukKuTheme
import com.example.viewmodel.KerupukViewModel
import com.example.viewmodel.KerupukViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: KerupukViewModel by viewModels {
        KerupukViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KerupukKuTheme {
                MainContainerScreen(viewModel = viewModel)
            }
        }
    }
}
