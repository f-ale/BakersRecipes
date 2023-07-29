package com.example.bakersrecipes.repositories

import com.example.bakersrecipes.data.Alarm
import com.example.bakersrecipes.data.AlarmDao
import com.example.bakersrecipes.data.Step
import com.example.bakersrecipes.data.StepDao
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
    private val alarmDao: AlarmDao,
    private val stepDao: StepDao
) {
private val stepStates: MutableMap<Int, MutableMap<Int, MutableStateFlow<StepState>>> = mutableMapOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            importSavedAlarms()
        }
    }

    suspend fun deleteStepsById(vararg stepIds: Int) =
        stepDao.deleteStepsById(*stepIds)

    suspend fun insertOrUpdateSteps(vararg steps: Step) =
        stepDao.insertOrUpdateSteps(*steps)

    /*
        Resumes timers that have been saved on the persistent storage.
     */
    suspend fun importSavedAlarms() {
        val alarms =
            alarmDao.getAllAlarms()

        alarms.groupBy { it.recipeId }.forEach { (recipeId, alarmsForRecipe) ->
            val steps = stepDao.getStepsForRecipeAsList(recipeId)
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
    /*
        Initializes the state data classes representing timer state.
     */
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
                    val alarm = alarmDao.getAlarm(id, recipeId) // get Alarm from the database

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

    /*
        Resumes a previously scheduled timer whose alarm has been stopped.
     */
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
    /*
        Sets a timer, starting the alarm and persisting it in permanent storage.
     */
    suspend fun setAlarm(recipeId:Int, stepId:Int) {
        stepStates[recipeId]?.let { stepStates ->
            stepStates[stepId]?.let { stepState ->
                stepState.value =
                    stepState.value.fromDuration(stepState.value.duration)

                stepState.value.scheduledTime?.let { scheduledTime ->
                    // save alarm to db
                    alarmDao.insertOrUpdate(
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

    /*
        Cancels a scheduled timer.
     */
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

        // Delete alarm from db
        alarmDao.delete(stepId, recipeId)
    }
}