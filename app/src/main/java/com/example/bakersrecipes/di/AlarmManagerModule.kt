package com.example.bakersrecipes.di

import android.app.AlarmManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.example.bakersrecipes.utils.AlarmUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AlarmManagerModule {
    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext appContext: Context): AlarmManager {
        return appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    @Provides
    @Singleton
    fun provideAlarmUtils(
        @ApplicationContext appContext: Context,
        notificationManager: NotificationManagerCompat
    ): AlarmUtil {
        return AlarmUtil(appContext, notificationManager)
    }
    // TODO: Move to somewhere else?
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext appContext: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(appContext)
    }
}