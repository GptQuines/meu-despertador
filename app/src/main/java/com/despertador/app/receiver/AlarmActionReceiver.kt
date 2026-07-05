package com.despertador.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.despertador.app.service.AlarmRingingService
import com.despertador.app.service.AlarmScheduler
import com.despertador.app.ui.alarmringing.AlarmRingingActivity

class AlarmActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0)

        when (action) {
            AlarmRingingService.ACTION_DISMISS -> {
                val serviceIntent = Intent(context, AlarmRingingService::class.java)
                context.stopService(serviceIntent)

                val closeIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra(EXTRA_ACTION, EXTRA_ACTION_DISMISS)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(closeIntent)
            }

            AlarmRingingService.ACTION_SNOOZE -> {
                val snoozeDuration = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, 5)

                val serviceIntent = Intent(context, AlarmRingingService::class.java)
                context.stopService(serviceIntent)

                val snoozeIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmScheduler.EXTRA_HOUR, intent.getIntExtra(AlarmScheduler.EXTRA_HOUR, 8))
                    putExtra(AlarmScheduler.EXTRA_MINUTE, intent.getIntExtra(AlarmScheduler.EXTRA_MINUTE, 0))
                    putExtra(AlarmScheduler.EXTRA_LABEL, intent.getStringExtra(AlarmScheduler.EXTRA_LABEL) ?: "")
                    putExtra(AlarmScheduler.EXTRA_VIBRATE, intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true))
                    putExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, true))
                    putExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, snoozeDuration)
                    putExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, intent.getIntExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, 3))
                    putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0))
                    putExtra(AlarmScheduler.EXTRA_DAYS, intent.getStringExtra(AlarmScheduler.EXTRA_DAYS) ?: "")
                    putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI))
                    putExtra(EXTRA_ACTION, EXTRA_ACTION_SNOOZE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(snoozeIntent)
            }
        }
    }

    companion object {
        const val EXTRA_ACTION = "action"
        const val EXTRA_ACTION_DISMISS = "dismiss"
        const val EXTRA_ACTION_SNOOZE = "snooze"
    }
}
