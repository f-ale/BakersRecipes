package com.example.bakersrecipes.ui.detail

import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.StepState
import com.example.bakersrecipes.data.datatypes.Percentage
import kotlinx.coroutines.flow.StateFlow
import java.math.BigDecimal

data class RecipeDetailState(
    val totalRecipeWeight:BigDecimal? = null,
    val ingredientDisplayList:List<Pair<String, Percentage>> = listOf(),
    val stepDisplayList:List<StateFlow<StepState>> = listOf(),
    val recipe: Recipe? = null
)