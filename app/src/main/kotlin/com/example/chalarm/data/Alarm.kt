package com.example.chalarm.data

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import kotlin.math.absoluteValue

@Keep
data class Alarm(
    val id: String = "",
    val name: String = "",
    val time: String = "",
    val repeatDays: List<String> = emptyList(),
    val toneUri: String? = null,
    val volume: Float = 1f,
    val muteOnStart: Boolean = false,
    val snoozeEnabled: Boolean = false,
    val snoozeTimeMinutes: Int = 5,
    val numChallenges: Int = 1,
    val challengeTypes: List<String> = emptyList(),
    var enabled: Boolean = true,

    val challengeConfigs: List<ChallengeConfig> = emptyList()

    ) {

    @get:Exclude
    val alarmIntId: Int
        get() = id.hashCode().absoluteValue

}
