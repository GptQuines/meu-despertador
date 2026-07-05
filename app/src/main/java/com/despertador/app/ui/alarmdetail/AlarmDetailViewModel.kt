package com.despertador.app.ui.alarmdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.despertador.app.data.model.Alarm
import com.despertador.app.data.repository.AlarmRepository
import com.despertador.app.service.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    private val alarmId: Long = savedStateHandle.get<Long>(AlarmScheduler.EXTRA_ALARM_ID) ?: -1L
    private var existingAlarm: Alarm? = null

    private val _hour = MutableStateFlow(8)
    val hour: StateFlow<Int> = _hour.asStateFlow()

    private val _minute = MutableStateFlow(0)
    val minute: StateFlow<Int> = _minute.asStateFlow()

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label.asStateFlow()

    private val _daysOfWeek = MutableStateFlow(emptySet<Int>())
    val daysOfWeek: StateFlow<Set<Int>> = _daysOfWeek.asStateFlow()

    private val _vibrate = MutableStateFlow(true)
    val vibrate: StateFlow<Boolean> = _vibrate.asStateFlow()

    private val _snoozeEnabled = MutableStateFlow(true)
    val snoozeEnabled: StateFlow<Boolean> = _snoozeEnabled.asStateFlow()

    private val _snoozeDuration = MutableStateFlow(5)
    val snoozeDuration: StateFlow<Int> = _snoozeDuration.asStateFlow()

    private val _maxSnoozes = MutableStateFlow(3)
    val maxSnoozes: StateFlow<Int> = _maxSnoozes.asStateFlow()

    private val _ringtoneUri = MutableStateFlow<String?>(null)
    val ringtoneUri: StateFlow<String?> = _ringtoneUri.asStateFlow()

    init {
        if (alarmId > 0) {
            loadAlarm()
        }
    }

    private fun loadAlarm() {
        viewModelScope.launch {
            val alarm = repository.getAlarmById(alarmId) ?: return@launch
            existingAlarm = alarm
            _hour.value = alarm.hour
            _minute.value = alarm.minute
            _label.value = alarm.label
            _daysOfWeek.value = parseDays(alarm.daysOfWeek)
            _vibrate.value = alarm.vibrate
            _snoozeEnabled.value = alarm.snoozeEnabled
            _snoozeDuration.value = alarm.snoozeDurationMin
            _maxSnoozes.value = alarm.maxSnoozes
            _ringtoneUri.value = alarm.ringtoneUri
        }
    }

    fun updateHour(h: Int) { _hour.value = h }
    fun updateMinute(m: Int) { _minute.value = m }
    fun updateLabel(l: String) { _label.value = l }
    fun toggleDay(day: Int) {
        val current = _daysOfWeek.value.toMutableSet()
        if (day in current) current.remove(day) else current.add(day)
        _daysOfWeek.value = current
    }
    fun updateVibrate(v: Boolean) { _vibrate.value = v }
    fun updateSnoozeEnabled(e: Boolean) { _snoozeEnabled.value = e }
    fun updateSnoozeDuration(d: Int) { _snoozeDuration.value = d }
    fun updateMaxSnoozes(m: Int) { _maxSnoozes.value = m }
    fun updateRingtoneUri(uri: String?) { _ringtoneUri.value = uri }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val alarm = Alarm(
                id = if (alarmId > 0) alarmId else 0,
                hour = _hour.value,
                minute = _minute.value,
                isEnabled = existingAlarm?.isEnabled ?: true,
                label = _label.value,
                daysOfWeek = _daysOfWeek.value.joinToString(","),
                vibrate = _vibrate.value,
                snoozeEnabled = _snoozeEnabled.value,
                snoozeDurationMin = _snoozeDuration.value,
                maxSnoozes = _maxSnoozes.value,
                snoozeCount = 0,
                ringtoneUri = _ringtoneUri.value
            )

            val savedAlarm = if (alarmId > 0) {
                repository.update(alarm)
                scheduler.cancel(existingAlarm ?: alarm)
                alarm
            } else {
                val newId = repository.insert(alarm)
                alarm.copy(id = newId)
            }

            if (savedAlarm.isEnabled) {
                scheduler.schedule(savedAlarm)
            }

            onSaved()
        }
    }

    private fun parseDays(daysOfWeek: String): Set<Int> {
        if (daysOfWeek.isBlank()) return emptySet()
        return daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    fun getRingtoneDisplayName(): String {
        val uri = _ringtoneUri.value ?: return "Toque padr\u00e3o"
        return "Toque personalizado"
    }
}
