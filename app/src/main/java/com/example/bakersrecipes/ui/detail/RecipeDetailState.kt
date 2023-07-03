package com.example.bakersrecipes.ui.detail

import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.StepState

data class RecipeDetailState(
    val totalRecipeWeight:Int? = null,
    val ingredientDisplayList:List<Pair<String, Float>> = listOf(),
    val stepDisplayList:List<StepState> = listOf(),
    val recipe: Recipe? = null
)