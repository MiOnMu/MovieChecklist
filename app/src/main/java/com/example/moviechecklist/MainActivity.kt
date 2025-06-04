package com.example.moviechecklist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.moviechecklist.ui.theme.MovieChecklistAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt Annotation
class MainActivity : ComponentActivity() {
    // ViewModelFactory is no longer needed here when using Hilt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieChecklistAppTheme {
                // MovieChecklistApp will get ViewModels using hiltViewModel()
                MovieChecklistApp()
            }
        }
    }
}