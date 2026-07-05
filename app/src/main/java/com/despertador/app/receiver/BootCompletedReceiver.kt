package com.despertador.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.despertador.app.data.repository.AlarmRepository
import com.despertador.app.service.AlarmScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val entryPoint = EntryPointAccessors.fromApplication(context, BootCompletedEntryPoint::class.java)
            val repository = entryPoint.repository()
            val scheduler = entryPoint.scheduler()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarms = repository.getEnabledAlarmsList()
                    scheduler.scheduleAllEnabled(alarms)
                } catch (_: Exception) { }
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootCompletedEntryPoint {
        fun repository(): AlarmRepository
        fun scheduler(): AlarmScheduler
    }
}
