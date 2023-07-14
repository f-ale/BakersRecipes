package com.example.bakersrecipes.repositories

import android.os.CountDownTimer
import com.example.bakersrecipes.data.Alarm
import com.example.bakersrecipes.data.AlarmState
import com.example.bakersrecipes.data.AlarmStates
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.Step
import com.example.bakersrecipes.data.StepState
import com.example.bakersrecipes.utils.AlarmUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class StepRepository @Inject constructor(
    private val alarmUtil: AlarmUtil,
    private val recipeDatabase: RecipeDatabase
) {
private val stepStates: MutableMap<Int, MutableMap<Int, MutableStateFlow<StepState>>> = mutableMapOf()
    // TODO: Or make them non suspending but launch coroutines
    private val alarmDao = recipeDatabase.alarmDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            importSavedAlarms()
        }
    }
    suspend fun importSavedAlarms() {
        val alarms =
            alarmDao.getAlarmsWithState(AlarmStates.SCHEDULED) +
                    alarmDao.getAlarmsWithState(AlarmStates.RINGING)

        val recipeIds = alarms.map { it.recipeId }.distinct()

        recipeIds.forEach {
                recipeId -> recipeDatabase.stepDao().getStepsForRecipe(recipeId).collect {
                steps -> initializeStepStates(steps)
            }
        }

        for (alarm in alarms) {
            val recipeId = alarm.recipeId
            val stepId = alarm.stepId

            // Restart the alarm
            setAlarm(recipeId, stepId)
        }
    }
    suspend fun initializeStepStates(steps: List<Step>): Map<Int, StateFlow<StepState>> {
        if(steps.isNotEmpty())
        {
            val recipeId = steps.first().recipeId
            if(!stepStates.containsKey(recipeId))
            {
                stepStates[recipeId] = mutableMapOf()
            }

            steps.forEach { step ->
                step.id?.let { id ->
                    val alarm = recipeDatabase.alarmDao().getAlarm(id, recipeId) // get Alarm from the database

                    var alarmState = AlarmState(step.duration.toLong(), AlarmStates.INACTIVE)
                    if (alarm != null) {
                        // calculate the remaining time based on the saved scheduledTime
                        val remainingTime = alarm.scheduledTime - System.currentTimeMillis()

                        if(remainingTime > 0L) {
                            alarmState = AlarmState(
                                remainingTime = remainingTime,
                                state = alarm.state
                            )
                        }
                    }

                    stepStates[recipeId]?.set(id, MutableStateFlow(
                        StepState(
                            stepId = id,
                            description = step.description,
                            duration = step.duration,
                            alarmState = alarmState
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

    suspend fun setAlarm(recipeId:Int, stepId:Int) {
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

                // save alarm to db
                recipeDatabase.alarmDao().insertOrUpdate(
                    Alarm(
                        stepId = stepId,
                        recipeId = recipeId,
                        state = AlarmStates.SCHEDULED,
                        scheduledTime = System.currentTimeMillis() + stepState.value.duration.toLong() * 60 * 1000
                    )
                )

                alarmUtil.setAlarm(recipeId, stepId, stepState.value.duration.roundToInt())
            }
        }
    }

    suspend fun cancelAlarm(recipeId:Int, stepId:Int, isAlarmRinging:Boolean = false) {
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

        // delete alarm from db
        recipeDatabase.alarmDao().delete(stepId, recipeId)
    }
}