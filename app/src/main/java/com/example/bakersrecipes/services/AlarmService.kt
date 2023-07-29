package com.example.bakersrecipes.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.bakersrecipes.R
import com.example.bakersrecipes.receivers.AlarmReceiver
import com.example.bakersrecipes.repositories.StepRepository
import com.example.bakersrecipes.utils.AlarmUtil.Companion.CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService: Service() {
    @Inject lateinit var stepRepository: StepRepository
    @Inject lateinit var powerManager: PowerManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val mediaPlayers: MutableMap<Pair<Int, Int>, MediaPlayer> = mutableMapOf()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        wakeLock = powerManager.run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BakersRecipes:Alarm").apply {
                acquire(10*60*1000L /*10 minutes*/)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmHashcode = intent?.getIntExtra("ALARM_HASHCODE", -1) ?: -1
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val recipeId = intent?.getIntExtra("RECIPE_ID", -1) ?: -1

        // Create an intent for the dismiss button
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
            putExtra("ALARM_HASHCODE", alarmHashcode)
            putExtra("ALARM_ID", alarmId)
            putExtra("RECIPE_ID", recipeId)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmHashcode,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val dismissNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm") // TODO: replace with step data
            .setContentText("Alarm is ringing")
            .setSmallIcon(R.drawable.bakers_recipes_logo)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Stop",
                dismissPendingIntent
            )
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()

        if (intent?.action == "Stop" || intent?.action == "DISMISS_ALARM") {
            mediaPlayers.forEach { (index, _) ->
                serviceScope.launch {
                    stepRepository.cancelAlarm(
                        stepId = index.first,
                        recipeId = index.second,
                        isAlarmRinging = true
                    )
                }
            }
            mediaPlayers.stopAllAndClear()
        }
        else {
            if (!mediaPlayers.containsKey(Pair(alarmId, recipeId))) {
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val mediaPlayer = MediaPlayer.create(this, alarmSound)
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM) // This usage is for alarms
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                mediaPlayer.isLooping = true
                mediaPlayer.start()

                mediaPlayers[Pair(alarmId, recipeId)] = mediaPlayer
            }
        }

        if(mediaPlayers.isEmpty())
        {
            stopSelf()
        }

        startForeground(alarmId, dismissNotification)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        mediaPlayers.stopAll()
        mediaPlayers.clear()
        wakeLock?.release()
        wakeLock = null
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
fun MutableMap<Pair<Int, Int>, MediaPlayer>.stopAll() {
    for (mediaPlayer in this.values) {
        mediaPlayer.stop()
    }
}

fun MutableMap<Pair<Int, Int>, MediaPlayer>.stopAllAndClear() {
    this.stopAll()
    this.clear()
}

