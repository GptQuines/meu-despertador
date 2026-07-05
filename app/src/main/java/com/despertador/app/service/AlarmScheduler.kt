package com.despertador.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.despertador.app.data.model.Alarm
import com.despertador.app.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        cancel(alarm)
        if (!alarm.isEnabled) return

        val days = parseDays(alarm.daysOfWeek)

        if (days.isEmpty()) {
            scheduleOnce(alarm)
        } else {
            days.forEach { day ->
                scheduleForDay(alarm, day)
            }
        }
    }

    private fun scheduleOnce(alarm: Alarm) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        setAlarm(calendar.timeInMillis, alarm)
    }

    private fun scheduleForDay(alarm: Alarm, dayOfWeek: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        setAlarm(calendar.timeInMillis, alarm)
    }

    private fun setAlarm(triggerTime: Long, alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_HOUR, alarm.hour)
            putExtra(EXTRA_MINUTE, alarm.minute)
            putExtra(EXTRA_LABEL, alarm.label)
            putExtra(EXTRA_VIBRATE, alarm.vibrate)
            putExtra(EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
            putExtra(EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMin)
            putExtra(EXTRA_MAX_SNOOZES, alarm.maxSnoozes)
            putExtra(EXTRA_SNOOZE_COUNT, alarm.snoozeCount)
            putExtra(EXTRA_DAYS, alarm.daysOfWeek)
            putExtra(EXTRA_RINGTONE_URI, alarm.ringtoneUri)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun snooze(alarm: Alarm, durationMin: Int = alarm.snoozeDurationMin) {
        val snoozeMillis = durationMin * 60 * 1000L
        val snoozeTime = System.currentTimeMillis() + snoozeMillis

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_HOUR, alarm.hour)
            putExtra(EXTRA_MINUTE, alarm.minute)
            putExtra(EXTRA_LABEL, alarm.label)
            putExtra(EXTRA_VIBRATE, alarm.vibrate)
            putExtra(EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
            putExtra(EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMin)
            putExtra(EXTRA_MAX_SNOOZES, alarm.maxSnoozes)
            putExtra(EXTRA_SNOOZE_COUNT, alarm.snoozeCount + 1)
            putExtra(EXTRA_DAYS, alarm.daysOfWeek)
            putExtra(EXTRA_RINGTONE_URI, alarm.ringtoneUri)
            putExtra(EXTRA_IS_SNOOZE, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarm.id + 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
    }

    fun rescheduleAll(alarms: List<Alarm>) {
        alarms.forEach { schedule(it) }
    }

    fun scheduleAllEnabled(alarms: List<Alarm>) {
        alarms.filter { it.isEnabled }.forEach { schedule(it) }
    }

    private fun parseDays(daysOfWeek: String): List<Int> {
        if (daysOfWeek.isBlank()) return emptyList()
        return daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_HOUR = "alarm_hour"
        const val EXTRA_MINUTE = "alarm_minute"
        const val EXTRA_LABEL = "alarm_label"
        const val EXTRA_VIBRATE = "alarm_vibrate"
        const val EXTRA_SNOOZE_ENABLED = "alarm_snooze_enabled"
        const val EXTRA_SNOOZE_DURATION = "alarm_snooze_duration"
        const val EXTRA_MAX_SNOOZES = "alarm_max_snoozes"
        const val EXTRA_SNOOZE_COUNT = "alarm_snooze_count"
        const val EXTRA_DAYS = "alarm_days"
        const val EXTRA_RINGTONE_URI = "alarm_ringtone_uri"
        const val EXTRA_IS_SNOOZE = "is_snooze"
    }
}
