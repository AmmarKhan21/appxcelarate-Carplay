package com.car.play.WeatherData

import android.app.Application
import android.content.Context
import com.car.play.GoogleAds.AppOpenAdManager
import com.car.play.GoogleAds.RemoteConfig
import com.car.play.android.app.reminders.NotificationHelper
import com.car.play.android.app.reminders.ReminderScheduler
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    companion object {
        lateinit var appOpenAdManager: AppOpenAdManager
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        RemoteConfig.setConfig(this)
        context = applicationContext
        appOpenAdManager = AppOpenAdManager(this)

        NotificationHelper.ensureChannel(this)
        ReminderScheduler.schedulePeriodic(this)
    }
}