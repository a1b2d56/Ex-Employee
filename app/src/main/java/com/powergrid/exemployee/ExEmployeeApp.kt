package com.powergrid.exemployee

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExEmployeeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Material You Dynamic Colors on Android 12+ (API 31+).
        // On Android 7-11 the static brand colours in themes.xml are used — no-op here.
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
