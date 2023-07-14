package com.example.bakersrecipes.repositories

import com.example.bakersrecipes.data.Alarm
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.Step
import com.example.bakersrecipes.data.StepState
import com.example.bakersrecipes.data.fromDuration
import com.example.bakersrecipes.utils.AlarmUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableMap
import javax.inject.Inject
import javax.inject.Singleton

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
            alarmDao.getAllAlarms()

        alarms.groupBy { it.recipeId }.forEach { (recipeId, alarmsForRecipe) ->
            val steps = recipeDatabase.stepDao().getStepsForRecipeAsList(recipeId)
            initializeStepStates(steps)

            for (alarm in alarmsForRecipe) {
                // Restart the alarm
                resumeAlarm(
                    recipeId = alarm.recipeId,
                    stepId = alarm.stepId,
                    scheduledTime = alarm.scheduledTime
                )
            }
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

                    var scheduledTime: Long? = null

                    if (alarm != null) {
                        scheduledTime = alarm.scheduledTime
                    }

                    stepStates[recipeId]?.set(id, MutableStateFlow(
                        StepState(
                            stepId = id,
                            description = step.description,
                            duration = step.duration,
                            scheduledTime = scheduledTime
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

    private fun resumeAlarm(recipeId: Int, stepId: Int, scheduledTime: Long) {
        stepStates[recipeId]?.let { stepStates ->
            stepStates[stepId]?.let { stepState ->
                stepState.value =
                    stepState.value.copy(
                        scheduledTime = scheduledTime
                    )

                stepState.value.scheduledTime?.let { scheduledTime ->
                    alarmUtil.setAlarm(recipeId, stepId, scheduledTime)
                }
            }
        }
    }
    suspend fun setAlarm(recipeId:Int, stepId:Int) {
        stepStates[recipeId]?.let { stepStates ->
            stepStates[stepId]?.let { stepState ->
                stepState.value =
                    stepState.value.fromDuration(stepState.value.duration)

                stepState.value.scheduledTime?.let { scheduledTime ->
                    // save alarm to db
                    recipeDatabase.alarmDao().insertOrUpdate(
                        Alarm(
                            stepId = stepId,
                            recipeId = recipeId,
                            scheduledTime = scheduledTime
                        )
                    )

                    alarmUtil.setAlarm(recipeId, stepId, scheduledTime)
                }
            }
        }
    }

    suspend fun cancelAlarm(recipeId:Int, stepId:Int, isAlarmRinging:Boolean = false) {
        stepStates[recipeId]?.let { stepStates ->
            stepStates[stepId]?.let { stepState ->
                stepState.value =
                    stepState.value.copy(
                        scheduledTime = null
                    )

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