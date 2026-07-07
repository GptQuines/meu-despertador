package com.despertador.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.despertador.app.data.model.Alarm
import com.despertador.app.service.AlarmRingingService
import com.despertador.app.service.AlarmScheduler
import com.despertador.app.ui.alarmringing.AlarmRingingActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0)
        if (alarmId == 0L) return

        val alarm = Alarm(
            id = alarmId,
            hour = intent.getIntExtra(AlarmScheduler.EXTRA_HOUR, 8),
            minute = intent.getIntExtra(AlarmScheduler.EXTRA_MINUTE, 0),
            label = intent.getStringExtra(AlarmScheduler.EXTRA_LABEL) ?: "",
            vibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true),
            snoozeEnabled = intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, true),
            snoozeDurationMin = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, 5),
            maxSnoozes = intent.getIntExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, 3),
            snoozeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0),
            daysOfWeek = intent.getStringExtra(AlarmScheduler.EXTRA_DAYS) ?: "",
            ringtoneUri = intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI)
        )

        // Reschedule for next occurrence if has repeat days
        if (alarm.daysOfWeek.isNotBlank()) {
            val entryPoint = EntryPointAccessors.fromApplication(context, AlarmSchedulerEntryPoint::class.java)
            entryPoint.scheduler().scheduleNext(alarm)
        }

        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_HOUR, alarm.hour)
            putExtra(AlarmScheduler.EXTRA_MINUTE, alarm.minute)
            putExtra(AlarmScheduler.EXTRA_LABEL, alarm.label)
            putExtra(AlarmScheduler.EXTRA_VIBRATE, alarm.vibrate)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMin)
            putExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, alarm.maxSnoozes)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, alarm.snoozeCount)
            putExtra(AlarmScheduler.EXTRA_DAYS, alarm.daysOfWeek)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, alarm.ringtoneUri)
            putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false))
        }
        context.startForegroundService(serviceIntent)

        val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_HOUR, alarm.hour)
            putExtra(AlarmScheduler.EXTRA_MINUTE, alarm.minute)
            putExtra(AlarmScheduler.EXTRA_LABEL, alarm.label)
            putExtra(AlarmScheduler.EXTRA_VIBRATE, alarm.vibrate)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMin)
            putExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, alarm.maxSnoozes)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, alarm.snoozeCount)
            putExtra(AlarmScheduler.EXTRA_DAYS, alarm.daysOfWeek)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, alarm.ringtoneUri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(activityIntent)
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmSchedulerEntryPoint {
        fun scheduler(): AlarmScheduler
    }
}
