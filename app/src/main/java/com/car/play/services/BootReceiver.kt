package com.car.play.android.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.car.play.android.app.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = AppDatabase.getDatabase(context).reminderDao().getActiveRemindersSync()
                ReminderScheduler.rescheduleAll(context, reminders)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
