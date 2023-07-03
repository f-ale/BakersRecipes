package com.example.bakersrecipes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.bakersrecipes.utils.AlarmUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {
    @Inject
    lateinit var alarmUtil: AlarmUtil
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        // Call cancelAlarm with the received alarmId
        if(alarmId != -1)
        {
            alarmUtil.cancelAlarm(alarmId)
        }

    }
}