package com.example.bakersrecipes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.bakersrecipes.repositories.StepRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver()
{
    @Inject
    lateinit var stepRepository: StepRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                stepRepository.importSavedAlarms() // TODO: Do we need this to be a service?
            }
        }
    }
}