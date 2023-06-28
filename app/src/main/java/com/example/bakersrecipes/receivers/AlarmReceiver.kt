package com.example.bakersrecipes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Play the ringing sound
        playRingingSound(context)
    }

    private fun playRingingSound(context: Context) {
        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone: Ringtone = RingtoneManager.getRingtone(context, ringtoneUri)

        // Start playing the ringing sound
        ringtone.play()
    }
}