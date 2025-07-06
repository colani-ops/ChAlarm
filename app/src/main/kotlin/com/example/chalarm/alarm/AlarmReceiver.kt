package com.example.chalarm.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.chalarm.util.AlarmScheduler
import com.example.chalarm.data.Alarm
import com.example.chalarm.ui.activities.AlarmActivity
import com.google.gson.Gson

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val alarmJson = intent.getStringExtra("alarmObject") ?: return
        val alarm = Gson().fromJson(alarmJson, Alarm::class.java)

        // Start AlarmService
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmService.EXTRA_ALARM_NAME, alarm.name)
            putExtra(AlarmService.EXTRA_ALARM_TONE_URI, alarm.toneUri)
        }
        context.startForegroundService(serviceIntent)

        // Launch AlarmActivity directly
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("alarmObject", alarmJson)
        }
        context.startActivity(activityIntent)

        // Reschedule if repeatDays are set
        if (alarm.repeatDays.isNotEmpty()) {
            val nextCalendar = AlarmScheduler.calculateNextOccurrence(alarm)
            if (nextCalendar != null) {
                AlarmScheduler.scheduleAlarm(context, alarm, nextCalendar)
            }
        }
    }
}
