package com.example.moviechecklist

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MovieChecklistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) { // BuildConfig.DEBUG is available if buildType has debuggable true
            Timber.plant(Timber.DebugTree())
        }
    }
}