package com.car.play.android.app.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val PERIODIC_WORK = "car_reminder_periodic"
    private const val ONE_SHOT_WORK = "car_reminder_oneshot"

    /** Schedules a once-a-day check. Safe to call repeatedly (KEEP policy). */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(12, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Runs an immediate check, e.g. right after the user adds a reminder. */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
