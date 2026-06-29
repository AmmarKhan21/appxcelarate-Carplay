package com.car.play.WeatherData

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.car.play.GoogleAds.AppOpenAdManager
import com.car.play.GoogleAds.RemoteConfig
import com.car.play.android.app.db.AppDatabase
import com.car.play.android.app.services.ReminderScheduler
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApp : Application() {
    companion object {
        lateinit var appOpenAdManager: AppOpenAdManager
        lateinit var context: Context
            private set
        const val REMINDER_CHANNEL_ID = "reminder_channel"
        const val FATIGUE_CHANNEL_ID = "fatigue_channel"
        const val GENERAL_CHANNEL_ID = "general_channel"
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        RemoteConfig.setConfig(this)
        context = applicationContext
        appOpenAdManager = AppOpenAdManager(this)
        MobileAds.initialize(this) {}
        createNotificationChannels()
        rescheduleReminders()
    }

    private fun rescheduleReminders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = AppDatabase.getDatabase(this@MyApp).reminderDao().getActiveRemindersSync()
                ReminderScheduler.rescheduleAll(this@MyApp, reminders)
            } catch (e: Exception) {
                android.util.Log.e("MyApp", "Failed to reschedule reminders", e)
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Car service and maintenance reminders"
                enableVibration(true)
            }

            val fatigueChannel = NotificationChannel(
                FATIGUE_CHANNEL_ID,
                "Fatigue Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Driver fatigue and break reminders"
                enableVibration(true)
            }

            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(fatigueChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
}