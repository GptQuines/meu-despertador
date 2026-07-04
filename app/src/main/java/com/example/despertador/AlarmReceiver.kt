package com.example.despertador

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarm = intent.getParcelableExtra<AlarmData>("alarm")

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("alarm", alarm)
        }
        context.startForegroundService(serviceIntent)

        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("alarm", alarm)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(activityIntent)
    }
}
