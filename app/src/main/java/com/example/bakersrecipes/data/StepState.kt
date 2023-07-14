package com.example.bakersrecipes.data

import android.os.CountDownTimer

data class AlarmState(
    val remainingTime: Long,
    val state: AlarmStates,
    val timer: CountDownTimer? = null
)
data class StepState(
    val stepId:Int,
    val description: String,
    val duration: Float,
    val alarmState: AlarmState = AlarmState(duration.toLong(), AlarmStates.INACTIVE),
)
enum class AlarmStates {
    INACTIVE,
    SCHEDULED,
    RINGING
}