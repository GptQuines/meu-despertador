package com.despertador.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.despertador.app.data.repository.AlarmRepository
import com.despertador.app.service.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: AlarmRepository

    @Inject
    lateinit var scheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_QUICKBOOT_POWERON ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarms = repository.getEnabledAlarmsList()
                    scheduler.scheduleAllEnabled(alarms)
                } catch (_: Exception) { }
            }
        }
    }
}
