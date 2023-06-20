package com.example.bakersrecipes

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.RecipeDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val db:RecipeDatabase,
    val dataStore: DataStore<Preferences>
): ViewModel()
{
    fun getAllRecipes(): Flow<List<Recipe>> =
        db.recipeDao().getAllRecipes()
}