package com.despertador.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.despertador.app.data.repository.AlarmRepository
import com.despertador.app.service.AlarmScheduler
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeZoneChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(context, TimeZoneChangedEntryPoint::class.java)
        val repository = entryPoint.repository()
        val scheduler = entryPoint.scheduler()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarms = repository.getEnabledAlarmsList()
                scheduler.rescheduleAll(alarms)
            } catch (_: Exception) { }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TimeZoneChangedEntryPoint {
        fun repository(): AlarmRepository
        fun scheduler(): AlarmScheduler
    }
}
