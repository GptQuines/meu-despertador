package com.despertador.app.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int = 8,
    val minute: Int = 0,
    val isEnabled: Boolean = true,
    val label: String = "",
    val daysOfWeek: String = "",
    val vibrate: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeDurationMin: Int = 5,
    val maxSnoozes: Int = 3,
    val snoozeCount: Int = 0,
    val ringtoneUri: String? = null
) : Parcelable
