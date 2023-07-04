package com.example.bakersrecipes.ui.detail

import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.StepState
import kotlinx.coroutines.flow.StateFlow

data class RecipeDetailState(
    val totalRecipeWeight:Int? = null,
    val ingredientDisplayList:List<Pair<String, Float>> = listOf(),
    val stepDisplayList:List<StateFlow<StepState>> = listOf(),
    val recipe: Recipe? = null
)