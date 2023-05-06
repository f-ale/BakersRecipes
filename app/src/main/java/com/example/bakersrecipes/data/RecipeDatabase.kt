package com.example.bakersrecipes.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.bakersrecipes.data.relations.RecipeWithIngredients

@Database(entities = [Recipe::class, Ingredient::class], version = 2)
abstract class RecipeDatabase: RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
}