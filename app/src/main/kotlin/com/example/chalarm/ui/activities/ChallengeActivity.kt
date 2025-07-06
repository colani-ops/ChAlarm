package com.example.chalarm.ui.activities

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chalarm.alarm.AlarmService
import com.example.chalarm.data.Alarm
import com.example.chalarm.data.ChallengeConfig
import com.example.chalarm.data.ChallengeType
import com.google.gson.Gson

// Compose
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView

// Lifecycle
/* import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.compose.LifecycleOwnerAmbient*/
import androidx.lifecycle.compose.LocalLifecycleOwner

// CameraX
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

// ContextCompat
import androidx.core.content.ContextCompat

// ML Kit
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.common.InputImage

// Compose Activity Results
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult

// Android
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.chalarm.ui.theme.ChAlarmTheme

class ChallengeActivity : ComponentActivity() {

    private lateinit var alarm: Alarm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmJson = intent.getStringExtra("alarmObject") ?: ""
        alarm = Gson().fromJson(alarmJson, Alarm::class.java)

        val queueJson = intent.getStringExtra("challengeQueue") ?: "[]"
        val remainingChallenges = Gson().fromJson(queueJson, Array<ChallengeConfig>::class.java).toMutableList()

        if (remainingChallenges.isEmpty()) {
            finishAlarm()
        } else {
            val currentChallenge = remainingChallenges.removeAt(0)
            setContent {
                ChAlarmTheme {
                    ChallengeScreen(currentChallenge) {
                        if (remainingChallenges.isEmpty()) {
                            finishAlarm()
                        } else {
                            // Launch next challenge
                            val nextQueueJson = Gson().toJson(remainingChallenges)
                            val nextIntent = Intent(this, ChallengeActivity::class.java).apply {
                                putExtra("alarmObject", Gson().toJson(alarm))
                                putExtra("challengeQueue", nextQueueJson)
                            }
                            startActivity(nextIntent)
                            finish()
                        }
                    }
                }
            }

        }
    }

    private fun finishAlarm() {
        sendStopAlarmIntent()
        finish()
    }

    private fun sendStopAlarmIntent() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        startService(stopIntent)
    }
}

@Composable
fun ChallengeScreen(challenge: ChallengeConfig, onSuccess: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Challenge: ${challenge.type.name}",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground
        )


        Spacer(modifier = Modifier.height(24.dp))

        when (challenge.type) {
            ChallengeType.RETYPE -> {
                RetypeChallengeScreen(challenge, onSuccess)
            }
            ChallengeType.MATH -> {
                MathChallengeScreen(challenge, onSuccess)
            }
            ChallengeType.FACE -> {
                FaceChallengeScreen(onSuccess)
            }
            ChallengeType.STEPS -> {
                StepsChallengeScreen(challenge, onSuccess)
            }
        }
    }
}



//STRING CHALLENGE
@Composable
fun RetypeChallengeScreen(challenge: ChallengeConfig, onSuccess: () -> Unit) {
    val length = challenge.retypeLength ?: 5
    val randomString = remember { generateRandomString(length) }
    var userInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Text(
        text = "Please type: $randomString",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = userInput,
        onValueChange = { userInput = it },
        label = { Text("Your input") },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii
        ),
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

    Spacer(modifier = Modifier.height(16.dp))

    if (showError) {
        Text(
            text = "Incorrect! Please try again.",
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    Button(onClick = {
        if (userInput == randomString) {
            onSuccess()
        } else {
            showError = true
        }
    }) {
        Text("Submit")
    }
}

fun generateRandomString(length: Int): String {
    val charset = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..length)
        .map { charset.random() }
        .joinToString("")
}


//MATH PROBLEM
data class MathProblem(
    val text: String,
    val answer: Int
)

@Composable
fun MathChallengeScreen(challenge: ChallengeConfig, onSuccess: () -> Unit) {
    val count = challenge.numProblems ?: 3
    val difficulty = challenge.difficulty ?: "Easy"
    val problems = remember { generateMathProblems(count, difficulty) }

    val userInputs = remember { mutableStateListOf<String>().apply { repeat(problems.size) { add("") } } }
    var showError by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        problems.forEachIndexed { index, problem ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = problem.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedTextField(
                value = userInputs[index],
                onValueChange = { userInputs[index] = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showError) {
            Text(
                "Some answers are incorrect. Please try again.",
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = {
            var allCorrect = true
            problems.forEachIndexed { i, prob ->
                val userAnswer = userInputs[i].toIntOrNull()
                if (userAnswer != prob.answer) allCorrect = false
            }
            if (allCorrect) {
                onSuccess()
            } else {
                showError = true
            }
        }) {
            Text("Submit")
        }
    }
}


fun generateMathProblems(count: Int, difficulty: String): List<MathProblem> {
    val problems = mutableListOf<MathProblem>()
    val operatorsEasy = listOf("+", "-")
    val operatorsNormal = listOf("+", "-", "*", "/")

    repeat(count) {
        val op = if (difficulty == "Normal") operatorsNormal.random() else operatorsEasy.random()
        val a = (10..99).random()
        val b = (1..20).random()

        val text: String
        val answer: Int

        when (op) {
            "+" -> {
                text = "$a + $b = ?"
                answer = a + b
            }
            "-" -> {
                text = "$a - $b = ?"
                answer = a - b
            }
            "*" -> {
                text = "$a × $b = ?"
                answer = a * b
            }
            "/" -> {
                val dividend = a * b // ensure divisible
                text = "$dividend ÷ $b = ?"
                answer = dividend / b
            }
            else -> {
                text = "$a + $b = ?"
                answer = a + b
            }
        }

        problems.add(MathProblem(text, answer))
    }

    return problems
}


@androidx.camera.core.ExperimentalGetImage
@Composable
fun FaceChallengeScreen(onSuccess: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val hasDetectedFace = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val detector = FaceDetection.getClient(
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .build()
            )

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    detector.process(inputImage)
                        .addOnSuccessListener { faces ->
                            if (faces.isNotEmpty() && !hasDetectedFace.value) {
                                hasDetectedFace.value = true
                                onSuccess()
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)

            previewView
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .background(Color.Black)
    )

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        "Align your face with the camera to dismiss the alarm.",
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground)
}




@Composable
fun StepsChallengeScreen(challenge: ChallengeConfig, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val targetSteps = challenge.targetSteps ?: 10
    var steps by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val accel = Math.sqrt((x * x + y * y + z * z).toDouble())
                if (accel > 30) { // Threshold for a shake / movement
                    steps++
                    if (steps >= targetSteps) {
                        onSuccess()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Steps / Movements: $steps / $targetSteps",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Move your phone or walk to complete the challenge.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}