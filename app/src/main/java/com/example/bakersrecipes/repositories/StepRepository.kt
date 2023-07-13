package com.example.bakersrecipes.repositories

import android.os.CountDownTimer
import com.example.bakersrecipes.data.AlarmStates
import com.example.bakersrecipes.data.Step
import com.example.bakersrecipes.data.StepState
import com.example.bakersrecipes.utils.AlarmUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.internal.toImmutableMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class StepRepository @Inject constructor(
    val alarmUtil: AlarmUtil
) {
private val stepStates: MutableMap<Int, MutableMap<Int, MutableStateFlow<StepState>>> = mutableMapOf()
    // TODO: getStepStates(recipeId)
    fun initializeStepStates(steps: List<Step>): Map<Int, StateFlow<StepState>> {
        if(steps.isNotEmpty())
        {
            val recipeId = steps.first().recipeId
            if(!stepStates.containsKey(recipeId))
            {
                stepStates[recipeId] = mutableMapOf()
            }

            steps.forEach { step ->
                step.id?.let {
                    stepStates[step.recipeId]?.set(step.id, MutableStateFlow(
                        StepState(
                            stepId = step.id,
                            description = step.description,
                            duration = step.duration
                        )
                    ))
                }
            }

            return stepStates[recipeId]?.toImmutableMap() ?: mapOf()
        }
        else {
            return mapOf()
        }
    }

    fun setAlarm(recipeId:Int, stepId:Int) {
        stepStates[recipeId]?.let { stepStates ->
            stepStates[stepId]?.let { stepState ->
                stepState.value =
                    stepState.value.copy(
                        alarmState = stepState.value.alarmState.copy(
                            state = AlarmStates.SCHEDULED
                        )
                    )

                val timer = object : CountDownTimer(stepState.value.duration.toLong() * 60 * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        stepState.value = stepState.value.copy(
                            alarmState = stepState.value.alarmState.copy(
                                remainingTime = millisUntilFinished
                            )
                        )
                    }

                    override fun onFinish() {
                        stepState.value = stepState.value.copy(
                            alarmState = stepState.value.alarmState.copy(
                                state = AlarmStates.RINGING
                            )
                        )
                    }
                }

                stepState.value = stepState.value.copy(
                    alarmState = stepState.value.alarmState.copy(
                        timer = timer
                    )
                )

                timer.start()

                alarmUtil.setAlarm(recipeId, stepId, stepState.value.duration.roundToInt())
            }
        }
    }

    fun cancelAlarm(recipeId:Int, stepId:Int, isAlarmRinging:Boolean = false) {
        stepStates[recipeId]?.let { stepStates ->
            stepStates[stepId]?.let { stepState ->
                stepState.value =
                    stepState.value.copy(
                        alarmState = stepState.value.alarmState.copy(
                            state = AlarmStates.INACTIVE
                        )
                    )

                stepState.value.alarmState.timer?.cancel()

                if(!isAlarmRinging)
                {
                    alarmUtil.cancelAlarm(recipeId, stepId)
                }
            }
        }
    }
}