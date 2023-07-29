package com.example.bakersrecipes.di

import android.content.Context
import androidx.room.Room
import com.example.bakersrecipes.data.AlarmDao
import com.example.bakersrecipes.data.IngredientDao
import com.example.bakersrecipes.data.RecipeDao
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.StepDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): RecipeDatabase {
        return Room.databaseBuilder(
            appContext,
            RecipeDatabase::class.java,
            "RecipeDatabase"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(recipeDatabase: RecipeDatabase): RecipeDao =
        recipeDatabase.recipeDao()

    @Provides
    @Singleton
    fun provideIngredientDao(recipeDatabase: RecipeDatabase): IngredientDao =
        recipeDatabase.ingredientDao()

    @Provides
    @Singleton
    fun provideStepDao(recipeDatabase: RecipeDatabase): StepDao =
        recipeDatabase.stepDao()

    @Provides
    @Singleton
    fun provideAlarmDao(recipeDatabase: RecipeDatabase): AlarmDao =
        recipeDatabase.alarmDao()
}