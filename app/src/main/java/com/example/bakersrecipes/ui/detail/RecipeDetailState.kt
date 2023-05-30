package com.example.bakersrecipes.ui.detail

import com.example.bakersrecipes.data.Recipe

data class RecipeDetailState(
    val totalRecipeWeight:Int? = null,
    val ingredientDisplayList:List<Pair<String, Float>> = listOf(),
    val recipe: Recipe? = null
)
