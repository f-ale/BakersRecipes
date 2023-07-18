package com.example.bakersrecipes.ui.detail

import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.Step
import com.example.bakersrecipes.data.datatypes.sumOf
import com.example.bakersrecipes.data.datatypes.toPercentage
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import com.example.bakersrecipes.repositories.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val db:RecipeDatabase,
    private val dataStore: DataStore<Preferences>,
    private val stepRepository: StepRepository,
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
                        stepDisplayList = stepRepository
                            .initializeStepStates(recipe.steps).values.toList()
                    )
                    // We want ingredients to be sorted by highest percentage
                    ingredients = recipe.ingredients.sortedByDescending { it.percent }
                    steps = recipe.steps
                }
            }
        }
    }
    fun setAlarm(stepId: Int) {
        viewModelScope.launch {
            stepRepository.setAlarm(recipeId, stepId)
        }
    }
    fun cancelAlarm(stepId: Int) {
        viewModelScope.launch {
            stepRepository.cancelAlarm(recipeId, stepId)
        }
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
                        { it.second.toUnformattedString() + "g" } // TODO: Move these operations to an utility class.
                        else { it.second.toString() + "%" }
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
        return dataStore.data.map {
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
                if(totalRecipeWeight.length < 15)
                {
                    updateMakeRecipeWeight(totalRecipeWeight.toBigDecimal())
                }
            } else {
                resetMakeRecipe()
            }
        } catch (_: NumberFormatException) {}
    }
    private fun updateMakeRecipeWeight(totalRecipeWeight:BigDecimal)
    {
        val percentSum = ingredients.sumOf { it.percent }

        val ingredientDisplayList = ingredients.map {
            Pair(it.name, ((it.percent / percentSum) * totalRecipeWeight.toPercentage()))
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
                    ingredient -> Pair(ingredient.name, ingredient.percent) // TODO: Don't repeat code
            },
            totalRecipeWeight = null
        )
    }
    private fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients?> =
        db.recipeDao().getRecipeWithIngredientsById(recipeId)
}

