package com.project.moviechecklist

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MovieChecklistApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        val workManager = WorkManager.getInstance(this)
        
        // Czyścimy stare zadania (np. to 7-dniowe), które zapisały się w pamięci telefonu
        workManager.cancelAllWork()
        
        // Zadanie testowe na prezentację: Uruchamia się 15 sekund po starcie aplikacji
        val testRequest = OneTimeWorkRequestBuilder<com.project.moviechecklist.worker.ReminderWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS)
            .build()
        
        workManager.enqueue(testRequest)
    }
}
