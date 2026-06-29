package com.car.play.android.app.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.car.play.android.app.Activities.HomeActivity
import com.car.play.android.app.db.ReminderEntity
import java.text.SimpleDateFormat
import java.util.Locale

object ReminderScheduler {

    private const val TAG = "ReminderScheduler"

    fun scheduleReminder(context: Context, reminder: ReminderEntity): Boolean {
        val triggerAt = parseTriggerTime(reminder.date, reminder.time) ?: return false
        if (triggerAt <= System.currentTimeMillis()) return false

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, reminder)

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    reminder.id.toInt(),
                    Intent(context, HomeActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAt, showIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
            true
        } catch (securityException: SecurityException) {
            Log.w(TAG, "setAlarmClock denied, falling back to inexact alarm", securityException)
            scheduleInexactAlarm(alarmManager, triggerAt, pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule reminder ${reminder.id}", e)
            scheduleInexactAlarm(alarmManager, triggerAt, pendingIntent)
        }
    }

    private fun scheduleInexactAlarm(
        alarmManager: AlarmManager,
        triggerAt: Long,
        pendingIntent: PendingIntent
    ): Boolean {
        return try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule inexact reminder alarm", e)
            false
        }
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(buildPendingIntent(context, reminderId))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel reminder $reminderId", e)
        }
    }

    suspend fun rescheduleAll(context: Context, reminders: List<ReminderEntity>) {
        reminders.filter { !it.isCompleted }.forEach { scheduleReminder(context, it) }
    }

    private fun buildPendingIntent(context: Context, reminder: ReminderEntity): PendingIntent {
        return buildPendingIntent(context, reminder.id, reminder.title, reminder.description)
    }

    private fun buildPendingIntent(context: Context, reminderId: Long): PendingIntent {
        return buildPendingIntent(context, reminderId, "", "")
    }

    private fun buildPendingIntent(
        context: Context,
        reminderId: Long,
        title: String,
        description: String
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("description", description)
            putExtra("reminder_id", reminderId.toInt())
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun parseTriggerTime(date: String, time: String): Long? {
        val patterns = listOf(
            "dd-MM-yyyy HH:mm",
            "dd/MM/yyyy HH:mm",
            "yyyy-MM-dd HH:mm",
            "dd-MM-yyyy hh:mm a",
            "dd/MM/yyyy hh:mm a"
        )
        val combined = "$date $time"
        for (pattern in patterns) {
            try {
                val formatter = SimpleDateFormat(pattern, Locale.getDefault())
                formatter.isLenient = false
                val parsed = formatter.parse(combined) ?: continue
                return parsed.time
            } catch (_: Exception) {
                // try next pattern
            }
        }
        return null
    }
}
