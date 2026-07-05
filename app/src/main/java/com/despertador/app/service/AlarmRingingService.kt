package com.despertador.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.despertador.app.DespertadorApplication
import com.despertador.app.receiver.AlarmActionReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmRingingService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var flashHandler: Handler? = null
    private var flashOn = false
    private var isRunning = false
    private var wakeLock: PowerManager.WakeLock? = null

    private var alarmId: Long = 0
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0
    private var alarmLabel: String = ""
    private var alarmVibrate: Boolean = true
    private var alarmSnoozeEnabled: Boolean = true
    private var alarmSnoozeDuration: Int = 5
    private var alarmMaxSnoozes: Int = 3
    private var alarmSnoozeCount: Int = 0
    private var alarmDays: String = ""
    private var alarmRingtoneUri: String? = null
    private var isSnooze: Boolean = false

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        cameraId = findFlashCameraId()
        flashHandler = Handler(Looper.getMainLooper())
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        isRunning = true
        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0)
        alarmHour = intent.getIntExtra(AlarmScheduler.EXTRA_HOUR, 8)
        alarmMinute = intent.getIntExtra(AlarmScheduler.EXTRA_MINUTE, 0)
        alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_LABEL) ?: ""
        alarmVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true)
        alarmSnoozeEnabled = intent.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, true)
        alarmSnoozeDuration = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, 5)
        alarmMaxSnoozes = intent.getIntExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, 3)
        alarmSnoozeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0)
        alarmDays = intent.getStringExtra(AlarmScheduler.EXTRA_DAYS) ?: ""
        alarmRingtoneUri = intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI)
        isSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false)

        try {
            startForeground(NOTIFICATION_ID, createNotification())
        } catch (_: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        startAlarmSound()
        if (alarmVibrate) startVibration()
        startFlash()
        scheduleAutoStop()
        return START_STICKY
    }

    private fun startAlarmSound() {
        try {
            val uri: Uri = if (!alarmRingtoneUri.isNullOrBlank()) {
                Uri.parse(alarmRingtoneUri)
            } else {
                val defaultAlarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                defaultAlarm ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        } catch (_: Exception) { }
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
                flashHandler?.postDelayed(this, 300)
            }
        })
    }

    private fun findFlashCameraId(): String? {
        return try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
                "Despertador:AlarmWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 1000L)
        } catch (_: Exception) { }
    }

    private fun scheduleAutoStop() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isRunning) stopAlarm(clearNotification = true)
        }, 10 * 60 * 1000L)
    }

    fun stopAlarm(clearNotification: Boolean = true) {
        isRunning = false
        flashHandler?.removeCallbacksAndMessages(null)
        turnOffFlash()
        ringtone?.stop()
        vibrator?.cancel()
        wakeLock?.release()
        if (clearNotification) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        stopSelf()
    }

    fun snoozeAlarm() {
        isRunning = false
        flashHandler?.removeCallbacksAndMessages(null)
        turnOffFlash()
        ringtone?.stop()
        vibrator?.cancel()
        wakeLock?.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun turnOffFlash() {
        try {
            if (cameraId != null) {
                cameraManager?.setTorchMode(cameraId!!, false)
            }
        } catch (_: Exception) { }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        flashHandler?.removeCallbacksAndMessages(null)
        turnOffFlash()
        ringtone?.stop()
        vibrator?.cancel()
        wakeLock?.release()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val dismissIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val snoozeIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_HOUR, alarmHour)
            putExtra(AlarmScheduler.EXTRA_MINUTE, alarmMinute)
            putExtra(AlarmScheduler.EXTRA_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_VIBRATE, alarmVibrate)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, alarmSnoozeEnabled)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, alarmSnoozeDuration)
            putExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, alarmMaxSnoozes)
            putExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, alarmSnoozeCount)
            putExtra(AlarmScheduler.EXTRA_DAYS, alarmDays)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, alarmRingtoneUri)
        }

        val pendingDismiss = PendingIntent.getBroadcast(
            this, alarmId.toInt(), dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pendingSnooze = PendingIntent.getBroadcast(
            this, (alarmId + 50000).toInt(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmDisplayName = alarmLabel.ifBlank { "Despertador" }
        val timeText = String.format("%02d:%02d", alarmHour, alarmMinute)

        return NotificationCompat.Builder(this, DespertadorApplication.CHANNEL_ALARM)
            .setContentTitle(" • ")
            .setContentText("Toque para abrir")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pendingDismiss, true)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Soneca", pendingSnooze)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Parar", pendingDismiss)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_DISMISS = "com.despertador.app.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.despertador.app.ACTION_SNOOZE"
    }
}
