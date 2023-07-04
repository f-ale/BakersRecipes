package com.example.bakersrecipes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.bakersrecipes.repositories.StepRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {
    @Inject
    lateinit var stepRepository: StepRepository
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val recipeId = intent.getIntExtra("RECIPE_ID", -1)
        // Call cancelAlarm with the received alarmId
        if(alarmId != -1 && recipeId != -1)
        {
            stepRepository.cancelAlarm(stepId = alarmId, recipeId = recipeId)
        }

    }
}