package com.example.chalarm.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.chalarm.alarm.AlarmService
import com.example.chalarm.data.Alarm
import com.example.chalarm.data.ChallengeConfig
import com.example.chalarm.ui.activities.ChallengeActivity
import com.example.chalarm.viewmodel.AlarmViewModel
import com.google.gson.Gson

@Composable
fun AlarmRingingScreen(
    alarm: Alarm,
    onDismiss: (List<ChallengeConfig>?) -> Unit,
    onSnooze:() -> Unit
) {

    val context = LocalContext.current
    val viewModel: AlarmViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Alarm: ${alarm.name}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(24.dp))

        // Dismiss button
        Button(
            onClick = {
            if (alarm.challengeConfigs.isEmpty()) {
                viewModel.dismissAlarm(alarm, context)
                onDismiss(null)
            } else {
                // Build challenge queue
                val total = alarm.numChallenges
                val randomizableConfigs =
                    alarm.challengeConfigs.filter {
                            it.type == com.example.chalarm.data.ChallengeType.RETYPE ||
                            it.type == com.example.chalarm.data.ChallengeType.MATH
                    }
                val guaranteedConfigs =
                    alarm.challengeConfigs.filter {
                            it.type == com.example.chalarm.data.ChallengeType.FACE ||
                            it.type == com.example.chalarm.data.ChallengeType.STEPS
                    }

                val challengeQueue = mutableListOf<ChallengeConfig>()

                if (randomizableConfigs.isNotEmpty()) {
                    repeat(total) {
                        val config = randomizableConfigs.random()
                        challengeQueue.add(config)
                    }
                }

                challengeQueue.addAll(guaranteedConfigs)

                onDismiss(challengeQueue)
            }
        },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                "Dismiss",
                color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (alarm.snoozeEnabled) {
            Button(onClick = {
                viewModel.snoozeAlarm(alarm, context)
                onSnooze()
            }) {
                Text(
                    "Snooze",
                    color = MaterialTheme.colorScheme.onSecondary
                    )
            }
        }
    }
}