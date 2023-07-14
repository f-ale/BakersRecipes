package com.example.bakersrecipes.data

val StepState.alarmState: AlarmStates
    get() =
        if(this.scheduledTime == null)
        {
            AlarmStates.INACTIVE
        } else if(this.scheduledTime > System.currentTimeMillis()) {
            AlarmStates.SCHEDULED
        } else {
            AlarmStates.RINGING
        }

fun StepState.fromDuration(duration: Float) : StepState =
    this.copy(scheduledTime = System.currentTimeMillis() + (duration.toLong() * 60 * 1000))

data class StepState(
    val stepId:Int,
    val description: String,
    val duration: Float,
    val scheduledTime: Long?
)
enum class AlarmStates { // TODO: AlarmStates can be eliminated and replaced with a nullable scheduledTime
    INACTIVE,
    SCHEDULED,
    RINGING
}