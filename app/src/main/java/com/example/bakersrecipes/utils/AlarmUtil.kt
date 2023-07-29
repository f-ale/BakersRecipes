package com.example.bakersrecipes.utils
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.bakersrecipes.receivers.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
class AlarmUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
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
            ).apply {
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC// allows to show notification on lock screen
                enableVibration(true)
                description = "Timers for recipe steps"
                setBypassDnd(true)
            }

            val notificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setAlarm(recipeId:Int, alarmId: Int, scheduledTime: Long) {
        val hashCode = ("$recipeId-$alarmId").hashCode()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_HASHCODE", hashCode)
            putExtra("ALARM_ID", alarmId)
            putExtra("RECIPE_ID", recipeId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTime,
                PendingIntent.getBroadcast(
                    context,
                    hashCode,
                    intent,
                    PendingIntent.FLAG_MUTABLE
                )
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                scheduledTime,
                PendingIntent.getBroadcast(
                    context,
                    hashCode,
                    intent,
                    PendingIntent.FLAG_MUTABLE
                )
            )
        }
    }

    fun cancelAlarm(recipeId:Int, alarmId: Int) {
        val hashCode = ("$recipeId-$alarmId").hashCode()
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                hashCode,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_HASHCODE", hashCode)
            putExtra("ALARM_ID", alarmId)
            putExtra("RECIPE_ID", recipeId)
            action = "Stop"
        }

        context.sendBroadcast(intent)
    }
    companion object {
         val CHANNEL_ID = "recipe_step_timer"
    }
}