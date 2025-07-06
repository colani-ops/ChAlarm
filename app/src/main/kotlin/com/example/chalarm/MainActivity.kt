package com.example.chalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.chalarm.ui.navigation.AppNavGraph
import com.example.chalarm.ui.theme.ChAlarmTheme
import com.example.chalarm.viewmodel.AlarmViewModel
import com.example.chalarm.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            ChAlarmTheme {
                val navController = rememberNavController()

                val alarmViewModel: AlarmViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()

                val startAlarmId = intent?.getStringExtra("alarmId")
                val startDestination = if (!startAlarmId.isNullOrEmpty()) {
                    "alarmRinging/$startAlarmId"
                } else {
                    "home"
                }

                AppNavGraph(
                    navController = navController,
                    alarmViewModel = alarmViewModel,
                    authViewModel = authViewModel,
                    startDestination = startDestination
                )
            }
        }
    }
}
