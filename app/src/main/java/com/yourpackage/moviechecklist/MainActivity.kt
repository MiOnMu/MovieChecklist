package com.yourpackage.moviechecklist

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.yourpackage.moviechecklist.ui.navigation.AppNavigation
import com.yourpackage.moviechecklist.ui.screens.common.BottomNavBar
import com.yourpackage.moviechecklist.ui.theme.MovieChecklistTheme // Create this theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieChecklistTheme { // Replace with your app's theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { BottomNavBar(navController = navController) }
                    ) { paddingValues ->
                        AppNavigation(navController = navController, paddingValues = paddingValues)
                    }
                }
            }
        }
    }
}