package com.despertador.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_HOUR, intent.getIntExtra(AlarmScheduler.EXTRA_HOUR, 8))
            putExtra(AlarmScheduler.EXTRA_MINUTE, intent.getIntExtra(AlarmScheduler.EXTRA_MINUTE, 0))
            putExtra(AlarmScheduler.EXTRA_LABEL, intent.getStringExtra(AlarmScheduler.EXTRA_LABEL) ?: "")
            putExtra(AlarmScheduler.EXTRA_VIBRATE, intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true))
            putExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, true))
            putExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, 5))
            putExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, intent.getIntExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, 3))
            putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0))
            putExtra(AlarmScheduler.EXTRA_DAYS, intent.getStringExtra(AlarmScheduler.EXTRA_DAYS) ?: "")
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI))
            putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false))
        }
        context.startForegroundService(serviceIntent)

        val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_HOUR, intent.getIntExtra(AlarmScheduler.EXTRA_HOUR, 8))
            putExtra(AlarmScheduler.EXTRA_MINUTE, intent.getIntExtra(AlarmScheduler.EXTRA_MINUTE, 0))
            putExtra(AlarmScheduler.EXTRA_LABEL, intent.getStringExtra(AlarmScheduler.EXTRA_LABEL) ?: "")
            putExtra(AlarmScheduler.EXTRA_VIBRATE, intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true))
            putExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, true))
            putExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, 5))
            putExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, intent.getIntExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, 3))
            putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0))
            putExtra(AlarmScheduler.EXTRA_DAYS, intent.getStringExtra(AlarmScheduler.EXTRA_DAYS) ?: "")
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(activityIntent)
    }
}
