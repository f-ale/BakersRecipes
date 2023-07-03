package com.example.bakersrecipes.utils
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bakersrecipes.R
import com.example.bakersrecipes.receivers.AlarmReceiver
import com.example.bakersrecipes.workers.AlarmWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val CHANNEL_ID = "recipe_step_timer"
private const val WORK_TAG_PREFIX = "alarmworker_"

class AlarmUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManagerCompat,
) {
    init {
        createNotificationChannel()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recipe Timers",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Timers for recipe steps"

            val notificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    @SuppressLint("MissingPermission")
    fun notify(stepId: Int, description: String) {
        // Create a unique request code for the dismiss button PendingIntent
        val requestCode = stepId + 1

        // Create an intent for the dismiss button
        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
            putExtra("ALARM_ID", stepId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification with the dismiss action button
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Timer ringing")
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Stop",
                dismissPendingIntent
            )
            .build()

        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(stepId, notification)
        }
    }

    fun setAlarm(alarmId: Int, minutes: Int) {
        val alarmWorkRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(minutes.toLong(), java.util.concurrent.TimeUnit.MINUTES)
            .addTag(WORK_TAG_PREFIX + alarmId)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_TAG_PREFIX + alarmId,
            ExistingWorkPolicy.REPLACE,
            alarmWorkRequest
        )
    }
    fun cancelAlarm(alarmId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG_PREFIX + alarmId)
        notificationManager.cancel(alarmId)
    }
}