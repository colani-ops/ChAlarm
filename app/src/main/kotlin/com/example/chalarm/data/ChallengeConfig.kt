package com.example.chalarm.data

data class ChallengeConfig(
    val type: ChallengeType = ChallengeType.RETYPE,
    val retypeLength: Int? = null, // For RETYPE
    val numProblems: Int? = null,  // For MATH
    val difficulty: String? = null, // For MATH ("Easy", "Normal")
    val targetSteps: Int? = null   // For STEPS
)
