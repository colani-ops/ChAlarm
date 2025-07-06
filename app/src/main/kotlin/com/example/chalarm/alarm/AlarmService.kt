package com.example.chalarm.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.chalarm.R

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 1

        const val EXTRA_ALARM_ID = "alarmId"
        const val EXTRA_ALARM_NAME = "alarmName"
        const val EXTRA_ALARM_TONE_URI = "alarmToneUri"
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == "STOP_ALARM") {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmName = intent?.getStringExtra(EXTRA_ALARM_NAME) ?: "Alarm"
        val toneUriString = intent?.getStringExtra(EXTRA_ALARM_TONE_URI)

        startForeground(NOTIFICATION_ID, buildNotification(alarmName))

        try {
            val toneUri = if (!toneUriString.isNullOrEmpty()) Uri.parse(toneUriString)
            else android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, toneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                if (intent?.getBooleanExtra("muteOnStart", false) == true) {
                    setVolume(0f, 0f)
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }


    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun buildNotification(alarmName: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm is ringing")
            .setContentText(alarmName)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {

            val name = "Alarm Channel"
            val descriptionText = "Channel for Alarm Service"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
    }
}
