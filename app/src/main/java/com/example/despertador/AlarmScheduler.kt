package com.example.despertador

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: AlarmData) {
        cancel(alarm)
        if (!alarm.isEnabled) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (alarm.daysOfWeek.isEmpty()) {
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            setAlarm(calendar.timeInMillis, alarm)
        } else {
            for (day in alarm.daysOfWeek) {
                val dayCalendar = calendar.clone() as Calendar
                dayCalendar.set(Calendar.DAY_OF_WEEK, day)
                if (dayCalendar.timeInMillis <= System.currentTimeMillis()) {
                    dayCalendar.add(Calendar.WEEK_OF_YEAR, 1)
                }
                setAlarm(dayCalendar.timeInMillis, alarm)
            }
        }
    }

    private fun setAlarm(timeInMillis: Long, alarm: AlarmData) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm", alarm)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }

    fun cancel(alarm: AlarmData) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
