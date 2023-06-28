package com.example.bakersrecipes.ui.detail

import android.content.Intent
import android.os.CountDownTimer
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.Step
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import com.example.bakersrecipes.utils.AlarmUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val alarmUtils: AlarmUtils,
    private val db:RecipeDatabase,
    private val dataStore: DataStore<Preferences>,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    val recipeId:Int = savedStateHandle.get<Int>("recipeId") ?: -1
    private val _recipeDetailState = MutableStateFlow(RecipeDetailState())
    private var ingredients: List<Ingredient> = listOf()
    private var steps: List<Step> = listOf()

    val recipeDetailState: StateFlow<RecipeDetailState> = _recipeDetailState.asStateFlow()

    init {
        viewModelScope.launch {
            getRecipeWithIngredientsById(recipeId).collect {
                recipe ->
                if(recipe != null)
                {
                    _recipeDetailState.value = RecipeDetailState(
                        recipe = recipe.recipe,
                        ingredientDisplayList = recipe.ingredients.map {
                                ingredient -> Pair(ingredient.name, ingredient.percent)
                        }.sortedByDescending { it.second },
                        stepDisplayList = recipe.steps.map {
                            step ->
                            StepState(
                                stepId = step.id ?: -1,
                                description = step.description,
                                duration = step.duration
                            )
                        }
                    )
                    // We want ingredients to be sorted by highest percentage
                    ingredients = recipe.ingredients.sortedByDescending { it.percent }
                    steps = recipe.steps
                }
            }
        }
    }
    private val timers: MutableMap<Int, CountDownTimer?> = mutableMapOf()

    fun setAlarm(stepId: Int, duration:Int) {
        val timer = object : CountDownTimer(duration.toLong() * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val updatedState = _recipeDetailState.value.copy(
                    stepDisplayList = _recipeDetailState.value.stepDisplayList.map { stepState ->
                        if (stepState.stepId == stepId) {
                            stepState.copy(
                                isActive = true,
                                remainingTime = millisUntilFinished
                            )
                        } else {
                            stepState
                        }
                    }
                )
                _recipeDetailState.value = updatedState
            }

            override fun onFinish() {
                val updatedState = _recipeDetailState.value.copy(
                    stepDisplayList = _recipeDetailState.value.stepDisplayList.map { stepState ->
                        if (stepState.stepId == stepId) {
                            stepState.copy(isActive = false)
                        } else {
                            stepState
                        }
                    }
                )
                alarmUtils.notify(
                    stepId,
                    _recipeDetailState.value
                        .stepDisplayList.find { it.stepId == stepId }?.description ?: "")
                _recipeDetailState.value = updatedState
            }
        }

        timers[stepId] = timer
        timer.start()
        val updatedState = _recipeDetailState.value.copy(
            stepDisplayList = _recipeDetailState.value.stepDisplayList.map { stepState ->
                if (stepState.stepId == stepId) {
                    stepState.copy(isActive = true)
                } else {
                    stepState
                }
            }
        )
        _recipeDetailState.value = updatedState

        alarmUtils.setAlarm(stepId, duration) // Set the alarm/notification
    }

    fun cancelAlarm(stepId: Int) {
        val timer = timers[stepId]
        timer?.cancel()
        timers.remove(stepId)
        val updatedState = _recipeDetailState.value.copy(
            stepDisplayList = _recipeDetailState.value.stepDisplayList.map { stepState ->
                if (stepState.stepId == stepId) {
                    stepState.copy(isActive = false, remainingTime = stepState.duration.toLong())
                } else {
                    stepState
                }
            }
        )
        _recipeDetailState.value = updatedState
        alarmUtils.cancelAlarm(stepId) // Cancel the alarm/notification
    }
    fun getShareIntent():Intent {
        val recipeName = recipeDetailState.value.recipe?.name ?: ""
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                recipeName + "\n" + recipeDetailState.value.ingredientDisplayList.map {
                    val second:String =
                        if(recipeDetailState.value.totalRecipeWeight != null)
                        { it.second.toInt().toString() + "g" } // TODO: Move these operations to an utility class
                        else { it.second.toInt().times(100).toString() + "%" }
                    it.first + " " + second + "\n"
                }.reduce { acc, it -> acc + it }
            )
            type = "text/plain"
        }

        return  Intent.createChooser(
            sendIntent,
            recipeName
        )
    }
    fun getWeightUnit(): Flow<String> {
        return dataStore.data.map { it ->
            if(it[booleanPreferencesKey("weight_unit")] == false)
                "g"
            else
                "oz"
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
    private fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients?> =
        db.recipeDao().getRecipeWithIngredientsById(recipeId)
}