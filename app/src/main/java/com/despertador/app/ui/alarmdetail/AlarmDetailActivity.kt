package com.despertador.app.ui.alarmdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.despertador.app.service.AlarmScheduler
import com.despertador.app.ui.theme.DespertadorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)

        setContent {
            DespertadorTheme {
                AlarmDetailScreen(
                    alarmId = alarmId,
                    onNavigateBack = { finish() }
                )
            }
        }
    }

    companion object {
        fun newInstance(context: Context, alarmId: Long) {
            val intent = Intent(context, AlarmDetailActivity::class.java).apply {
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            }
            context.startActivity(intent)
        }
    }
}
