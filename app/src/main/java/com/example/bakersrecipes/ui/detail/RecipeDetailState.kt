package com.example.bakersrecipes.ui.detail

import com.example.bakersrecipes.data.Recipe

data class RecipeDetailState(
    val totalRecipeWeight:Int? = null,
    val ingredientDisplayList:List<Pair<String, Float>> = listOf(),
    val stepDisplayList:List<StepState> = listOf(),
    val recipe: Recipe? = null
)
data class StepState(
    val stepId:Int,
    val description: String,
    val duration: Float,
    val timerState: TimerState = TimerState.INACTIVE,
    val remainingTime: Long = duration.toLong(),
)

enum class TimerState {
    INACTIVE,
    SCHEDULED,
    RINGING
}