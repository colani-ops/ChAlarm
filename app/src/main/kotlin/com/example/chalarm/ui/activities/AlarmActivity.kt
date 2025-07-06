package com.example.chalarm.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chalarm.alarm.AlarmService
import com.example.chalarm.data.Alarm
import com.example.chalarm.ui.screens.AlarmRingingScreen
import com.example.chalarm.ui.theme.ChAlarmTheme
import com.google.gson.Gson

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmJson = intent.getStringExtra("alarmObject") ?: ""
        val alarm = Gson().fromJson(alarmJson, Alarm::class.java)

        // Start AlarmService to play the tone
        val serviceIntent = Intent(this, AlarmService::class.java).apply {
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmService.EXTRA_ALARM_NAME, alarm.name)
            putExtra(AlarmService.EXTRA_ALARM_TONE_URI, alarm.toneUri)
            putExtra("muteOnStart", alarm.muteOnStart)
        }
        startForegroundService(serviceIntent)

        setContent {
            ChAlarmTheme {
                AlarmRingingScreen(
                    alarm = alarm,
                    onDismiss = { challengeQueue ->
                        if (challengeQueue == null) {
                            stopAlarm()
                            finish()
                        } else {
                            // Launch ChallengeActivity
                            val challengeQueueJson = Gson().toJson(challengeQueue)
                            val challengeIntent =
                                Intent(this, ChallengeActivity::class.java).apply {
                                    putExtra("alarmObject", Gson().toJson(alarm))
                                    putExtra("challengeQueue", challengeQueueJson)
                                }
                            startActivity(challengeIntent)
                            finish()
                        }
                    },
                    onSnooze = {
                        stopAlarm()
                        finish()
                    }
                )
            }
        }
    }

    private fun stopAlarm() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        startService(stopIntent)
    }
}
