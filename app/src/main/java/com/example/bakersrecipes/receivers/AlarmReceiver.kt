package com.example.bakersrecipes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.bakersrecipes.repositories.StepRepository
import com.example.bakersrecipes.services.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {
    @Inject
    lateinit var stepRepository: StepRepository

    override fun onReceive(context: Context, intent: Intent) {
        val alarmHashcode = intent.getIntExtra("ALARM_HASHCODE", -1)
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val recipeId = intent.getIntExtra("RECIPE_ID", -1)

        val alarmServiceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_HASHCODE", alarmHashcode)
            putExtra("ALARM_ID", alarmId)
            putExtra("RECIPE_ID", recipeId)
            action = intent.action
        }

        if(intent.action == "Stop" || intent.action == "DISMISS_ALARM") {
            context.startService(alarmServiceIntent)
            stepRepository.cancelAlarm(recipeId, alarmId, true)
        } else {
            ContextCompat.startForegroundService(context, alarmServiceIntent)
        }
    }
}