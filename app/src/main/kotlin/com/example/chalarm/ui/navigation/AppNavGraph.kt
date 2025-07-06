package com.example.chalarm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chalarm.ui.screens.*
import com.example.chalarm.viewmodel.AlarmViewModel
import com.example.chalarm.viewmodel.AuthViewModel
import androidx.navigation.NavHostController

@Composable
fun AppNavGraph(
    navController: NavHostController,
    alarmViewModel: AlarmViewModel,
    authViewModel: AuthViewModel,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            if (authViewModel.isUserLoggedIn()) {
                alarmViewModel.loadAlarms()
                HomeScreen(navController, alarmViewModel, authViewModel)
            } else {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }


        composable("createAlarm") {
            CreateAlarmScreen(navController, alarmViewModel)
        }

        composable(
            "editAlarm/{alarmId}",
            arguments = listOf(navArgument("alarmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId") ?: ""
            EditAlarmScreen(navController, alarmViewModel, alarmId)
        }

        composable("login") {
            LoginScreen(navController, authViewModel)
        }

        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
    }
}
