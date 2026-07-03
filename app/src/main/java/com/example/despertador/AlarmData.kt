package com.example.despertador

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlarmData(
    val id: Long = System.currentTimeMillis(),
    val hour: Int = 8,
    val minute: Int = 0,
    val isEnabled: Boolean = true,
    val label: String = "Despertador",
    val daysOfWeek: Set<Int> = emptySet()
) : Parcelable
