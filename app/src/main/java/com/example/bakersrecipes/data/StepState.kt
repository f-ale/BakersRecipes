package com.example.bakersrecipes.data

data class AlarmState(
    val scheduledTime: Long,
    val state: AlarmStates
) {
    companion object {
        fun fromDuration(duration: Float, state: AlarmStates): AlarmState {
           return AlarmState(
               System.currentTimeMillis() + (duration.toLong() * 60 * 1000),
               state
           )
        }
    }
}
data class StepState(
    val stepId:Int,
    val description: String,
    val duration: Float,
    val alarmState: AlarmState = AlarmState.fromDuration(duration, AlarmStates.INACTIVE)
)
enum class AlarmStates { // TODO: AlarmStates can be eliminated and replaced with a nullable scheduledTime
    INACTIVE,
    SCHEDULED,
    RINGING
}