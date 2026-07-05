package com.despertador.app.ui.alarmringing

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.despertador.app.service.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        dismissKeyguard()

        setContent {
            AlarmRingingScreen(
                alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0) ?: 0,
                hour = intent?.getIntExtra(AlarmScheduler.EXTRA_HOUR, 8) ?: 8,
                minute = intent?.getIntExtra(AlarmScheduler.EXTRA_MINUTE, 0) ?: 0,
                label = intent?.getStringExtra(AlarmScheduler.EXTRA_LABEL) ?: "",
                vibrate = intent?.getBooleanExtra(AlarmScheduler.EXTRA_VIBRATE, true) ?: true,
                snoozeEnabled = intent?.getBooleanExtra(AlarmScheduler.EXTRA_SNOOZE_ENABLED, true) ?: true,
                snoozeDuration = intent?.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_DURATION, 5) ?: 5,
                maxSnoozes = intent?.getIntExtra(AlarmScheduler.EXTRA_MAX_SNOOZES, 3) ?: 3,
                snoozeCount = intent?.getIntExtra(AlarmScheduler.EXTRA_SNOOZE_COUNT, 0) ?: 0,
                days = intent?.getStringExtra(AlarmScheduler.EXTRA_DAYS) ?: "",
                ringtoneUri = intent?.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI),
                onDismiss = { dismiss() },
                onSnooze = { snooze() }
            )
        }
    }

    private fun dismissKeyguard() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            }
        } catch (_: Exception) { }
    }

    private fun dismiss() {
        val serviceIntent = Intent(this, com.despertador.app.service.AlarmRingingService::class.java)
        stopService(serviceIntent)
        finishAndRemoveTask()
    }

    private fun snooze() {
        val serviceIntent = Intent(this, com.despertador.app.service.AlarmRingingService::class.java)
        stopService(serviceIntent)
        finishAndRemoveTask()
    }

    override fun onBackPressed() {
        // Block back button
    }
}
