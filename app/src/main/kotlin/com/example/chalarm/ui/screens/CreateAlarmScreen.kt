package com.example.chalarm.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.chalarm.util.AlarmScheduler
import com.example.chalarm.data.Alarm
import com.example.chalarm.data.ChallengeConfig
import com.example.chalarm.data.ChallengeType
import com.example.chalarm.viewmodel.AlarmViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(
    navController: NavHostController,
    alarmViewModel: AlarmViewModel,
    existingAlarm: Alarm? = null
) {
    var name by remember { mutableStateOf(existingAlarm?.name ?: "") }
    var hour by remember { mutableStateOf(existingAlarm?.time?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 7) }
    var minute by remember { mutableStateOf(existingAlarm?.time?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0) }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selectedDays = remember {
        mutableStateMapOf<String, Boolean>().apply {
            days.forEach { day ->
                this[day] = existingAlarm?.repeatDays?.contains(day) == true
            }
        }
    }

    var volume by remember { mutableFloatStateOf(existingAlarm?.volume ?: 0.5f) }
    var muteOnStart by remember { mutableStateOf(existingAlarm?.muteOnStart ?: false) }
    var snoozeEnabled by remember { mutableStateOf(existingAlarm?.snoozeEnabled ?: true) }
    var snoozeSlider by remember { mutableStateOf((existingAlarm?.snoozeTimeMinutes ?: 3).toFloat()) }
    var numChallenges by remember { mutableStateOf(existingAlarm?.numChallenges ?: 1) }

    val challengeOptions = listOf("Retype", "Math", "Face", "Steps")
    val selectedChallenges = remember {
        mutableStateMapOf<String, Boolean>().apply {
            challengeOptions.forEach { challenge ->
                this[challenge] = existingAlarm?.challengeTypes?.contains(challenge) == true
            }
        }
    }

    var toneUri by remember { mutableStateOf(existingAlarm?.toneUri ?: "") }

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val pickToneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                toneUri = uri.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var retypeLengthSlider by remember { mutableStateOf(5f) }
    var mathNumProblems by remember { mutableStateOf(3f) }
    var mathDifficulty by remember { mutableStateOf("Easy") }
    var stepsCountSlider by remember { mutableStateOf(15f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            if (existingAlarm == null) "Create Alarm" else "Edit Alarm",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Alarm Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Hour: $hour",
            color = MaterialTheme.colorScheme.onBackground
        )
        Slider(
            value = hour.toFloat(),
            onValueChange = { hour = it.toInt() },
            valueRange = 0f..23f,
            steps = 22,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )

        Text(
            "Minute: $minute",
            color = MaterialTheme.colorScheme.onBackground
        )
        Slider(
            value = minute.toFloat(),
            onValueChange = { minute = it.toInt() },
            valueRange = 0f..59f,
            steps = 58,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Repeat Days",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Column {
            days.chunked(4).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { day ->
                        FilterChip(
                            selected = selectedDays[day] == true,
                            onClick = { selectedDays[day] = !(selectedDays[day] ?: false) },
                            label = { Text(day) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { pickToneLauncher.launch(arrayOf("audio/*")) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Choose Custom Sound", color = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "Selected tone: ${if (toneUri.isNotEmpty()) toneUri else "Default"}",
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Volume: ${(volume * 100).toInt()}%",
            color = MaterialTheme.colorScheme.onBackground
        )
        Slider(
            value = volume,
            onValueChange = { volume = it },
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = muteOnStart, onCheckedChange = { muteOnStart = it })
            Text("Mute on Start", color = MaterialTheme.colorScheme.onBackground)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = snoozeEnabled, onCheckedChange = { snoozeEnabled = it })
            Text("Enable Snooze", color = MaterialTheme.colorScheme.onBackground)
        }

        Text(
            "Snooze Duration: ${snoozeSlider.toInt()} minutes",
            color = MaterialTheme.colorScheme.onBackground
        )
        Slider(
            value = snoozeSlider,
            onValueChange = { snoozeSlider = it },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Number of Challenges: $numChallenges",
            color = MaterialTheme.colorScheme.onBackground
        )
        Slider(
            value = numChallenges.toFloat(),
            onValueChange = { numChallenges = it.toInt().coerceAtLeast(1) },
            valueRange = 1f..5f,
            steps = 4,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Challenge Types",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Column {
            challengeOptions.chunked(3).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { challenge ->
                        FilterChip(
                            selected = selectedChallenges[challenge] == true,
                            onClick = { selectedChallenges[challenge] = !(selectedChallenges[challenge] ?: false) },
                            label = { Text(challenge) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Challenge Config Sliders
        if (selectedChallenges["Retype"] == true) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Retype length: ${retypeLengthSlider.toInt()}", color = MaterialTheme.colorScheme.onBackground)
            Slider(
                value = retypeLengthSlider,
                onValueChange = { retypeLengthSlider = it },
                valueRange = 3f..10f,
                steps = 7,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (selectedChallenges["Math"] == true) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Math problems: ${mathNumProblems.toInt()}", color = MaterialTheme.colorScheme.onBackground)
            Slider(
                value = mathNumProblems,
                onValueChange = { mathNumProblems = it },
                valueRange = 1f..5f,
                steps = 4,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )
            Text("Math difficulty: $mathDifficulty", color = MaterialTheme.colorScheme.onBackground)
            Row {
                Button(onClick = { mathDifficulty = "Easy" }) { Text("Easy") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { mathDifficulty = "Normal" }) { Text("Normal") }
            }
        }

        if (selectedChallenges["Steps"] == true) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Steps target: ${stepsCountSlider.toInt()}",
                color = MaterialTheme.colorScheme.onBackground)
            Slider(
                value = stepsCountSlider,
                onValueChange = { stepsCountSlider = it },
                valueRange = 10f..30f,
                steps = 20 ,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // Save
                val formattedTime = "%02d:%02d".format(hour, minute)
                val configs = mutableListOf<ChallengeConfig>()
                if (selectedChallenges["Retype"] == true) {
                    configs.add(ChallengeConfig(type = ChallengeType.RETYPE, retypeLength = retypeLengthSlider.toInt()))
                }
                if (selectedChallenges["Math"] == true) {
                    configs.add(ChallengeConfig(type = ChallengeType.MATH, numProblems = mathNumProblems.toInt(), difficulty = mathDifficulty))
                }
                if (selectedChallenges["Face"] == true) {
                    configs.add(ChallengeConfig(type = ChallengeType.FACE))
                }
                if (selectedChallenges["Steps"] == true) {
                    configs.add(ChallengeConfig(type = ChallengeType.STEPS, targetSteps = stepsCountSlider.toInt()))
                }

                val updatedAlarm = Alarm(
                    id = existingAlarm?.id ?: UUID.randomUUID().toString(),
                    name = name,
                    time = formattedTime,
                    repeatDays = selectedDays.filterValues { it }.keys.toList(),
                    toneUri = toneUri,
                    volume = volume,
                    muteOnStart = muteOnStart,
                    snoozeEnabled = snoozeEnabled,
                    snoozeTimeMinutes = snoozeSlider.toInt(),
                    numChallenges = numChallenges,
                    challengeTypes = selectedChallenges.filterValues { it }.keys.toList(),
                    challengeConfigs = configs,
                    enabled = true
                )

                if (existingAlarm == null) alarmViewModel.addAlarm(updatedAlarm)
                else alarmViewModel.updateAlarm(updatedAlarm)

                AlarmScheduler.scheduleAlarm(navController.context, updatedAlarm)
                navController.navigate("home") { popUpTo("home") { inclusive = true } }
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Save Alarm", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
