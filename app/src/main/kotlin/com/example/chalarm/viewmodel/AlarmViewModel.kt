package com.example.chalarm.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chalarm.data.Alarm
import com.example.chalarm.util.FirebaseHelper
import com.example.chalarm.util.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms

    fun loadAlarms() {
        viewModelScope.launch {
            FirebaseHelper.listenToAlarms { newList ->
                _alarms.value = newList
            }
        }
    }

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            FirebaseHelper.addAlarm(alarm)
            AlarmScheduler.scheduleAlarm(getApplication(), alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            FirebaseHelper.updateAlarm(alarm) {
                loadAlarms()
            }
            AlarmScheduler.scheduleAlarm(getApplication(), alarm)
        }
    }


    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            FirebaseHelper.deleteAlarm(alarm.id)
            AlarmScheduler.cancelAlarm(getApplication(), alarm)
        }
    }

    fun deleteAlarmById(id: String) {
        alarms.value.find { it.id == id }?.let {
            deleteAlarm(it)
        }
    }

    fun toggleAlarmEnabled(alarm: Alarm, enabled: Boolean) {
        val updatedAlarm = alarm.copy(enabled = enabled)
        updateAlarm(updatedAlarm)
    }


    fun dismissAlarm(alarm: Alarm, context: Context) {
            AlarmScheduler.cancelAlarm(context, alarm)
    }

    fun snoozeAlarm(alarm: Alarm, context: Context) {
            AlarmScheduler.scheduleSnooze(context, alarm)
    }
}
