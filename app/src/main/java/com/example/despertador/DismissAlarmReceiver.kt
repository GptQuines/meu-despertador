package com.example.despertador

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DismissAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, AlarmService::class.java)
        context.stopService(serviceIntent)
    }
}
