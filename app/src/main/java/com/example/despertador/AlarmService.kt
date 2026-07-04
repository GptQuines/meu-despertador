package com.example.despertador

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var flashHandler: Handler? = null
    private var flashOn = false
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        cameraId = try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager?.getCameraCharacteristics(id)
                chars?.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) { null }

        flashHandler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        val alarm = intent?.getParcelableExtra<AlarmData>("alarm")
        try {
            startForeground(NOTIFICATION_ID, createNotification(alarm?.label ?: "Despertador"))
        } catch (_: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }
        startAlarm()
        startFlash()
        scheduleAutoStop()
        return START_STICKY
    }

    private fun startAlarm() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        } catch (_: Exception) { }

        startVibration()
    }

    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
            }
        } catch (_: Exception) { }
    }

    private fun startFlash() {
        if (cameraId == null) return
        flashHandler?.post(object : Runnable {
            override fun run() {
                if (!isRunning) return
                try {
                    cameraManager?.setTorchMode(cameraId!!, flashOn)
                    flashOn = !flashOn
                } catch (_: Exception) { }
                flashHandler?.postDelayed(this, 400)
            }
        })
    }

    private fun scheduleAutoStop() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isRunning) stopAlarm()
        }, 5 * 60 * 1000L)
    }

    fun stopAlarm() {
        isRunning = false
        flashHandler?.removeCallbacksAndMessages(null)
        try { cameraManager?.setTorchMode(cameraId ?: return, false) } catch (_: Exception) { }
        ringtone?.stop()
        vibrator?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        flashHandler?.removeCallbacksAndMessages(null)
        try { cameraManager?.setTorchMode(cameraId ?: return, false) } catch (_: Exception) { }
        ringtone?.stop()
        vibrator?.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Alarme", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Notificação do despertador" }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun createNotification(label: String): Notification {
        val dismissIntent = Intent(this, DismissAlarmReceiver::class.java)
        val pendingDismiss = PendingIntent.getBroadcast(
            this, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarme")
            .setContentText(label)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dispensar", pendingDismiss)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1
    }
}
