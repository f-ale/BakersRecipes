package com.example.bakersrecipes.data

val StepState.alarmState: AlarmState
    get() =
        if(this.scheduledTime == null)
        {
            AlarmState.INACTIVE
        } else if(this.scheduledTime > System.currentTimeMillis()) {
            AlarmState.SCHEDULED
        } else {
            AlarmState.RINGING
        }

fun StepState.fromDuration(duration: Float) : StepState =
    this.copy(scheduledTime = System.currentTimeMillis() + (duration.toLong() * 60 * 1000))

data class StepState(
    val stepId:Int,
    val description: String,
    val duration: Float,
    val scheduledTime: Long?
)
enum class AlarmState {
    INACTIVE,
    SCHEDULED,
    RINGING
}