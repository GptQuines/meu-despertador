package com.despertador.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DespertadorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM,
            "Alarmes",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificações de alarme tocando"
            setSound(null, null)
            enableVibration(true)
        }

        val generalChannel = NotificationChannel(
            CHANNEL_GENERAL,
            "Geral",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificações gerais do aplicativo"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(alarmChannel)
        notificationManager.createNotificationChannel(generalChannel)
    }

    companion object {
        const val CHANNEL_ALARM = "alarm_channel"
        const val CHANNEL_GENERAL = "general_channel"
    }
}
