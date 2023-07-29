package com.example.bakersrecipes.repositories

import androidx.room.withTransaction
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.IngredientDao
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.RecipeDao
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDatabase: RecipeDatabase,
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao
) {
    suspend fun deleteRecipe(recipe: Recipe) =
        recipeDao.delete(recipe)

    suspend fun insertOrUpdate(recipe: Recipe) =
        recipeDao.insertOrUpdate(recipe)

    fun getRecipeIdByName(recipeName: String) =
        recipeDao.getRecipeIdByName(recipeName)

    suspend fun deleteIngredientsById(vararg ingredientId: Int) =
        ingredientDao.deleteIngredientsById(*ingredientId)

    suspend fun insertOrUpdateIngredients(vararg ingredients: Ingredient) =
        ingredientDao.insertOrUpdateIngredients(*ingredients)

    suspend fun <R> withTransaction(action: suspend () -> R): R =
        recipeDatabase.withTransaction(action)

    fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients?> =
        recipeDao.getRecipeWithIngredientsById(recipeId)
}