package com.example.bakersrecipes.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.NumberFormatException
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val db:RecipeDatabase,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    val recipeId:Int = savedStateHandle.get<Int>("recipeId") ?: -1
    private val _recipeDetailState = MutableStateFlow(RecipeDetailState())
    private var ingredients: List<Ingredient> = listOf()

    val recipeDetailState: StateFlow<RecipeDetailState> = _recipeDetailState.asStateFlow()

    init {
        viewModelScope.launch {
            getRecipeWithIngredientsById(recipeId).collect {
                recipe -> _recipeDetailState.value = RecipeDetailState(
                    recipe = recipe.recipe,
                    ingredientDisplayList = recipe.ingredients.map {
                        ingredient -> Pair(ingredient.name, ingredient.percent)
                    }.sortedByDescending { it.second }
                )
                // We want ingredients to be sorted by highest percentage
                ingredients = recipe.ingredients.sortedByDescending { it.percent }
            }
        }
    }

    fun updateMakeRecipeWeightFromString(totalRecipeWeight: String)
    {
        try {
            if(totalRecipeWeight != "") {
                updateMakeRecipeWeight(totalRecipeWeight.toInt())
            } else {
                resetMakeRecipe()
            }
        } catch (_: NumberFormatException) {}
    }

    private fun updateMakeRecipeWeight(totalRecipeWeight:Int)
    {
        val percentSum = ingredients.sumOf { it.percent.toDouble() }

        val ingredientDisplayList = ingredients.map {
            Pair(it.name, ((it.percent / percentSum) * totalRecipeWeight).toFloat())
        }

        _recipeDetailState.value = _recipeDetailState.value.copy(
            totalRecipeWeight = totalRecipeWeight,
            ingredientDisplayList = ingredientDisplayList
        )
    }

    private fun resetMakeRecipe()
    {
        _recipeDetailState.value = _recipeDetailState.value.copy(
            ingredientDisplayList = ingredients.map {
                    ingredient -> Pair(ingredient.name, ingredient.percent)
            },
            totalRecipeWeight = null
        )
    }

    private fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients> =
        db.recipeDao().getRecipeWithIngredientsById(recipeId)
}