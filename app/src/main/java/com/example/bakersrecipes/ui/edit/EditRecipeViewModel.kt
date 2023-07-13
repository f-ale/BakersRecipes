package com.example.bakersrecipes.ui.edit

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.Step
import com.example.bakersrecipes.data.datatypes.Percentage
import com.example.bakersrecipes.data.datatypes.toPercentage
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val db:RecipeDatabase,
    dataStore: DataStore<Preferences>,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val recipeId: Int? = savedStateHandle.get<Int>("recipeId")
    private val _editRecipeState = MutableStateFlow(EditRecipeState())
    private var recipe: RecipeWithIngredients? = null

    val editRecipeState: StateFlow<EditRecipeState> = _editRecipeState.asStateFlow()

    val weightUnit = dataStore.data.map { it ->
        if(it[booleanPreferencesKey("weight_unit")] == false)
            "g"
        else
            "oz"
    }

    val showWeight = dataStore.data.map {
        it[booleanPreferencesKey("show_weight")] ?: false
    }
    init {
        if (recipeId != null) {
            viewModelScope.launch {
                getRecipeWithIngredientsById(recipeId).collect { it ->
                    if(it != null) {
                        recipe = it
                        _editRecipeState.value =
                            EditRecipeState(
                                title = it.recipe.name,
                                description = it.recipe.description,
                                image = it.recipe.image,
                                ingredients = ingredientsToIngredientField(it.ingredients)
                                    .sortedByDescending { it.percent?.toBigDecimal() },
                                steps = stepsToStepField(it.steps)                            )
                    }
                }
            }
        }
    }
    fun deleteRecipe()
    {
        viewModelScope.launch {
            recipe?.recipe?.let {
                db.recipeDao().delete(it)
            }
        }
    }
    fun saveChanges(): Boolean
    {
        val validForm =
            editRecipeState.value.title.isNullOrEmpty().not() &&
            editRecipeState.value.description.isNullOrEmpty().not() &&
            editRecipeState.value.ingredients.all {
                it.percent.isNullOrEmpty().not() && it.name.isNullOrEmpty().not()
            } && editRecipeState.value.steps.all {
                it.description.isNullOrEmpty().not() && it.duration.isNullOrEmpty().not()
            }

        if(validForm) {
            viewModelScope.launch {
                db.withTransaction {
                    var recipeId: Int? = recipe?.recipe?.id
                    val recipeName = editRecipeState.value.title ?: "Untitled"
                    val recipeImage = editRecipeState.value.image
                    val recipeDescription = editRecipeState.value.description

                    val newRecipe =
                        Recipe(
                            id = recipeId,
                            name = recipeName,
                            image = recipeImage,
                            description = recipeDescription
                        )

                    db.recipeDao().insertOrUpdate(newRecipe)
                    // TODO: Replace Room's upsert with custom method that returns new object id

                    if(recipeId == null)
                    {
                        recipeId = db.recipeDao().getRecipeIdByName(recipeName) // TODO: Unclean
                        // TODO: this will be useless when we replace room upsert with something better
                    }

                    val ingredientDao = db.ingredientDao()

                    val ingredientsToRemove = editRecipeState.value
                        .removedIngredients.map{ it.ingredientId ?: -1 }

                    ingredientDao.deleteIngredientsById(*ingredientsToRemove.toIntArray())

                    if(editRecipeState.value.ingredients.isNotEmpty())
                    {
                        val maxPercentage = editRecipeState.value.ingredients.maxOf {
                            it.percent?.toPercentage(true) ?: "0".toPercentage(true)
                        }

                        val ingredientsToUpdate = editRecipeState.value.ingredients.map { field ->
                            Ingredient(
                                id = field.ingredientId,
                                recipeId = recipeId,
                                name = field.name ?: "Untitled",
                                percent =
                                field.percent
                                    ?.toPercentage(isPercentageString = true)
                                    ?.div(maxPercentage)
                                    ?: Percentage("-1"),
                            )
                        }

                        ingredientDao.insertOrUpdateIngredients(*ingredientsToUpdate.toTypedArray())
                    }

                    val stepDao = db.stepDao()
                    val stepsToRemove = editRecipeState.value
                        .removedSteps.map{ it.stepId ?: -1 }

                    stepDao.deleteStepsById(*stepsToRemove.toIntArray())

                    if(editRecipeState.value.steps.isNotEmpty())
                    {


                        val stepsToUpdate = editRecipeState.value.steps.map {
                                field ->
                            Step(
                                id = field.stepId,
                                recipeId = recipeId,
                                description = field.description ?: "Untitled",
                                duration = field.duration?.toFloatOrNull() ?: 0f,
                            )

                        }

                       stepDao.insertOrUpdateSteps(*stepsToUpdate.toTypedArray())
                    }
                }
            }
        }
        return validForm
    }
    fun newIngredient()
    {
        _editRecipeState.value = _editRecipeState.value.copy(
            ingredients = (_editRecipeState.value.ingredients.toMutableList() +
                EditRecipeIngredientField()).toList()
        )
    }
    fun newStep()
    {
        _editRecipeState.value = _editRecipeState.value.copy(
            steps = (_editRecipeState.value.steps.toMutableList() +
                    EditRecipeStepField()).toList()
        )
    }
    fun updateImage(uri: Uri?)
    {
        if(uri != null)
        {
            _editRecipeState.value = _editRecipeState.value.copy(image = uri)
        }
    }
    fun updateIngredient(index: Int, new:EditRecipeIngredientField)
    {
        if(isStringAValidBigDecimalOrEmptyOrNull(new.percent))
        {
            val mutableIngredientList = _editRecipeState.value.ingredients.toMutableList()

            mutableIngredientList[index] = new

            _editRecipeState.value = _editRecipeState.value.copy(
                ingredients = mutableIngredientList.toList()
            )
        }
    }
    fun updateStep(index: Int, new:EditRecipeStepField)
    {
        if(isStringAValidBigDecimalOrEmptyOrNull(new.duration))
        {
            val mutableStepList = _editRecipeState.value.steps.toMutableList()

            mutableStepList[index] = new

            _editRecipeState.value = _editRecipeState.value.copy(
                steps = mutableStepList.toList()
            )
        }
    }
    private fun isStringAValidBigDecimalOrEmptyOrNull(number: String?): Boolean
    {
        if(number == null || number == "")
            return true

        return try {
            number.toBigDecimal().toString()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    fun updateName(newName:String)
    {
        _editRecipeState.value = _editRecipeState.value.copy(title = newName)
    }
    fun updateDescription(newDescription:String)
    {
        _editRecipeState.value = _editRecipeState.value.copy(description = newDescription)
    }
    fun removeIngredient(ingredient:EditRecipeIngredientField)
    {
        val mutableIngredientList = _editRecipeState.value.ingredients.toMutableList()
        mutableIngredientList.remove(ingredient)

        val mutableRemovedIngredientList = _editRecipeState.value.removedIngredients.toMutableList()
        mutableRemovedIngredientList.add(ingredient)

        _editRecipeState.value = _editRecipeState.value.copy(
            ingredients = mutableIngredientList,
            removedIngredients = mutableRemovedIngredientList
        )
    }
    fun removeStep(step:EditRecipeStepField)
    {
        val mutableStepList = _editRecipeState.value.steps.toMutableList()
        mutableStepList.remove(step)

        val mutableRemovedStepList = _editRecipeState.value.removedSteps.toMutableList()
        mutableRemovedStepList.add(step)

        _editRecipeState.value = _editRecipeState.value.copy(
            steps = mutableStepList,
            removedSteps = mutableRemovedStepList
            // TODO: Use a single list for both, with a "deleted" flag on the data, and then filter the data according to its use?
        )
    }
    private fun ingredientsToIngredientField(ingredients: List<Ingredient>): List<EditRecipeIngredientField>
    {
        return ingredients.sortedBy{it.id}.map {
            ingredient ->
            EditRecipeIngredientField(
                name = ingredient.name,
                percent = ingredient.percent.toString(),
                ingredientId = ingredient.id,
            )
        }
    }
    private fun stepsToStepField(steps: List<Step>): List<EditRecipeStepField>
    {
        return steps.sortedBy{it.id}.map {

                step ->
            EditRecipeStepField(
                description = step.description,
                duration = step.duration.toString(),
                stepId = step.id,
            )
        }
    }
    private fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients?> =
        db.recipeDao().getRecipeWithIngredientsById(recipeId)
}