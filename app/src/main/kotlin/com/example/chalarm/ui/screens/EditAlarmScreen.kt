package com.example.chalarm.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.example.chalarm.viewmodel.AlarmViewModel
import com.example.chalarm.util.FirebaseHelper

@Composable
fun EditAlarmScreen(
    navController: NavHostController,
    alarmViewModel: AlarmViewModel,
    alarmId: String
) {
    var alarm by remember { mutableStateOf<com.example.chalarm.data.Alarm?>(null) }

    LaunchedEffect(alarmId) {
        FirebaseHelper.getAlarmOnce(alarmId) {
            alarm = it
        }
    }

    if (alarm == null) {
        Text("Loading alarm...")
        return
    }

    // Reuse CreateAlarmScreen with the loaded alarm as "edit mode"
    CreateAlarmScreen(
        navController = navController,
        alarmViewModel = alarmViewModel,
        existingAlarm = alarm
    )
}
