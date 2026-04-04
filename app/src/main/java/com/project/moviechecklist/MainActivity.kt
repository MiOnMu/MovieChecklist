package com.project.moviechecklist

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.project.moviechecklist.ui.navigation.AppNavigation
import com.project.moviechecklist.ui.screens.common.BottomNavBar
import com.project.moviechecklist.ui.theme.MovieChecklistTheme
import com.project.moviechecklist.worker.ReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkNotificationPermission()

        setContent {
            MovieChecklistTheme {
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

    override fun onStop() {
        super.onStop()
        // Kiedy użytkownik wychodzi z apki na pulpit (onStop), 
        // planujemy powiadomienie za 15 sekund.
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }

    override fun onStart() {
        super.onStart()
        // Kiedy użytkownik wraca do apki (onStart), 
        // anulujemy zaplanowane powiadomienie, żeby nie przeszkadzało w trakcie używania.
        WorkManager.getInstance(this).cancelAllWork()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
