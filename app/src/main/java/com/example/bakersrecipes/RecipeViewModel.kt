package com.example.bakersrecipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val db:RecipeDatabase
): ViewModel()
{
    fun getAllRecipes(): Flow<List<Recipe>> =
        db.recipeDao().getAllRecipes()

    fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients?> =
        db.recipeDao().getRecipeWithIngredientsById(recipeId)

    fun getIngredientsForRecipe(recipeId: Int): Flow<List<Ingredient>>
    {
        return db.ingredientDao().getIngredientsForRecipe(recipeId)
    }

    fun insertRecipe(recipe: Recipe)
    {
        viewModelScope.launch {
            db.recipeDao().insert(recipe)
        }
    }
}