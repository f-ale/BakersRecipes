package com.example.bakersrecipes.workers

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AlarmWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        val defaultUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone: Ringtone = RingtoneManager.getRingtone(context, defaultUri)

        try {
            playRingingSound(ringtone)
        } finally {
            ringtone.stop()
        }

        Result.success()
    }

    private suspend fun playRingingSound(ringtone: Ringtone) {
        ringtone.play()

        // Stop the ringing sound after 5 minutes (300,000 milliseconds)
        val stopDelay = 300000L
        delay(stopDelay)

        ringtone.stop()
    }
}