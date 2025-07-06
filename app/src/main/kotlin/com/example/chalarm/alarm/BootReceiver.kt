package com.example.chalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.chalarm.util.FirebaseHelper
import com.example.chalarm.util.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d("BootReceiver", "Boot completed detected — restoring alarms")

        FirebaseHelper.listenToAlarms { alarms ->
            alarms.filter { it.enabled }.forEach { alarm ->
                AlarmScheduler.scheduleAlarm(context, alarm)
                Log.d("BootReceiver", "Rescheduled alarm: ${alarm.name}")
            }
        }
    }
}
