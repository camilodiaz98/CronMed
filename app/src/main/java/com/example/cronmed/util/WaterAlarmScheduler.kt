package com.example.cronmed.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.cronmed.receiver.WaterAlarmReceiver
import java.util.*

class WaterAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleWaterReminders(timesPerDay: Int) {
        cancelAll()
        if (timesPerDay <= 0) return

        val intervalMinutes = if (timesPerDay > 1) (12 * 60) / (timesPerDay - 1) else 0
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        for (i in 0 until timesPerDay) {
            val intent = Intent(context, WaterAlarmReceiver::class.java).apply {
                putExtra("REMINDER_ID", i)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1000 + i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            var triggerTime = calendar.timeInMillis
            if (triggerTime < System.currentTimeMillis()) {
                triggerTime += 24 * 60 * 60 * 1000 // Next day
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            
            calendar.add(Calendar.MINUTE, intervalMinutes)
        }
    }

    fun cancelAll() {
        for (i in 0 until 12) { // Cancel up to 12 potential reminders
            val intent = Intent(context, WaterAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1000 + i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}
