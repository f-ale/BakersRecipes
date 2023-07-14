package com.example.bakersrecipes.di

import android.app.AlarmManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.repositories.StepRepository
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
    fun provideStepRepository(
        alarmUtil: AlarmUtil,
        recipeDatabase: RecipeDatabase
    ): StepRepository {
        return StepRepository(alarmUtil, recipeDatabase)
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext appContext: Context
    ): WorkManager {
        return WorkManager.getInstance(appContext)
    }
    @Provides
    @Singleton
    fun provideAlarmUtils(
        @ApplicationContext appContext: Context,
        alarmManager: AlarmManager,
    ): AlarmUtil {
        return AlarmUtil(
            context = appContext,
            alarmManager = alarmManager
        )
    }
    // TODO: Move to somewhere else?
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext appContext: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(appContext)
    }
}