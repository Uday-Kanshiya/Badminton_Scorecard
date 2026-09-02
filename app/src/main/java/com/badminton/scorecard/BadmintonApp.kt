package com.badminton.scorecard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BadmintonApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
