package com.example.chalarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.chalarm.alarm.AlarmReceiver
import com.example.chalarm.data.Alarm
import com.google.gson.Gson
import java.util.Calendar
import kotlin.math.absoluteValue

class AlarmScheduler {

    companion object {

        fun scheduleAlarm(context: Context, alarm: Alarm, customTime: Calendar? = null)
        {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Cannot schedule exact alarms — permission not granted!")
                return
            }

            val timeParts = alarm.time.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: return

            val calendar = customTime ?: Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val alarmJson = Gson().toJson(alarm)

            val intent = Intent(context, com.example.chalarm.ui.activities.AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("alarmObject", alarmJson)
            }

            val requestCode = alarm.alarmIntId

            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            Log.d("AlarmScheduler", "Scheduling alarm ID: ${alarm.id} for ${calendar.time}")

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        fun calculateNextOccurrence(alarm: Alarm): Calendar? {
            if (alarm.repeatDays.isEmpty()) return null

            val daysOfWeek = listOf(
                "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
            )

            val repeatIndices = alarm.repeatDays.mapNotNull { daysOfWeek.indexOf(it).takeIf { i -> i >= 0 } }

            val now = Calendar.getInstance()
            val currentDay = now.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sun

            val timeParts = alarm.time.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return null
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: return null

            // Find the soonest next repeat day
            for (i in 1..7) {
                val candidateDay = (currentDay + i) % 7
                if (repeatIndices.contains(candidateDay)) {
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, i)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    return cal
                }
            }
            return null
        }

        fun cancelAlarm(context: Context, alarm: Alarm) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, AlarmReceiver::class.java)

            val requestCode = alarm.alarmIntId

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d("AlarmScheduler", "Cancelled alarm ID: ${alarm.id}")
        }

        fun scheduleSnooze(context: Context, alarm: Alarm) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Cannot schedule exact alarms — permission not granted!")
                return
            }

            val triggerAtMillis = System.currentTimeMillis() + (alarm.snoozeTimeMinutes * 60 * 1000)

            val alarmJson = Gson().toJson(alarm)

            val snoozeRequestCode = (alarm.alarmIntId + System.currentTimeMillis().toInt()).absoluteValue

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarmObject", alarmJson)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                snoozeRequestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            Log.d("AlarmScheduler", "Scheduling snooze for ${alarm.name} at $triggerAtMillis")

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}