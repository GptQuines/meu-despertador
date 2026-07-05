package com.despertador.app.data.repository

import com.despertador.app.data.db.AlarmDao
import com.despertador.app.data.model.Alarm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {
    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()

    fun getEnabledAlarms(): Flow<List<Alarm>> = alarmDao.getEnabledAlarms()

    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getAlarmById(id)

    suspend fun insert(alarm: Alarm): Long = alarmDao.insert(alarm)

    suspend fun update(alarm: Alarm) = alarmDao.update(alarm)

    suspend fun delete(alarm: Alarm) = alarmDao.delete(alarm)

    suspend fun toggleEnabled(id: Long, enabled: Boolean) {
        alarmDao.toggleEnabled(id, enabled)
    }

    suspend fun updateSnoozeCount(id: Long, count: Int) {
        alarmDao.updateSnoozeCount(id, count)
    }

    suspend fun resetSnoozeCount(id: Long) {
        alarmDao.resetSnoozeCount(id)
    }

    suspend fun getEnabledAlarmsList(): List<Alarm> = alarmDao.getEnabledAlarmsList()
}
