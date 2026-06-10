package com.car.play.android.app.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.car.play.android.app.db.AppDatabase
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

/**
 * Runs periodically (and on demand) to surface reminders that are due, or that fall
 * within their "remind me" lead window. Once a reminder has been notified it is flagged
 * so the user is not spammed every day.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val dao = AppDatabase.getDatabase(applicationContext).reminderDao()
            val now = System.currentTimeMillis()
            val reminders = dao.getAllOnce()

            reminders.forEach { reminder ->
                if (reminder.notified) return@forEach

                val leadMillis = TimeUnit.DAYS.toMillis(reminder.leadDays.toLong())
                val notifyFrom = reminder.dueDateMillis - leadMillis

                if (now >= notifyFrom) {
                    val daysLeft = ceil(
                        (reminder.dueDateMillis - now).toDouble() / TimeUnit.DAYS.toMillis(1)
                    ).toInt()

                    val message = when {
                        daysLeft < 0 -> "${reminder.title} is overdue. Tap to review."
                        daysLeft == 0 -> "${reminder.title} is due today."
                        daysLeft == 1 -> "${reminder.title} is due tomorrow."
                        else -> "${reminder.title} is due in $daysLeft days."
                    }

                    NotificationHelper.show(
                        applicationContext,
                        reminder.id.toInt(),
                        reminder.title,
                        message
                    )
                    dao.markNotified(reminder.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
