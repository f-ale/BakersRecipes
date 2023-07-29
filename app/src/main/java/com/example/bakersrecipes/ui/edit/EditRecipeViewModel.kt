package com.example.bakersrecipes.ui.edit

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.Step
import com.example.bakersrecipes.data.datatypes.Percentage
import com.example.bakersrecipes.data.datatypes.toPercentage
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import com.example.bakersrecipes.repositories.RecipeRepository
import com.example.bakersrecipes.repositories.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val stepRepository: StepRepository,
    dataStore: DataStore<Preferences>,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val recipeId: Int? = savedStateHandle.get<Int>("recipeId")
    private val _editRecipeState = MutableStateFlow(EditRecipeState())
    private var recipe: RecipeWithIngredients? = null

    val editRecipeState: StateFlow<EditRecipeState> = _editRecipeState.asStateFlow()

    /*
        Gets the preferred units from user preferences
     */
    val weightUnit = dataStore.data.map { preference ->
        if(preference[booleanPreferencesKey("weight_unit")] == false)
            "g"
        else
            "oz"
    }

    /*
        Gets the show weight preference
     */
    val showWeight = dataStore.data.map { preference ->
        preference[booleanPreferencesKey("show_weight")] ?: false
    }

    /*
        Initializes the state representation of the edit recipe form
     */
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

    /*
        Deletes the current recipe
     */
    fun deleteRecipe()
    {
        viewModelScope.launch {
            recipe?.recipe?.let {
                recipeRepository.deleteRecipe(it)
            }
        }
    }
    /*
        Saves the user's changes according to the edited form
     */
    fun saveChanges(): Boolean
    {
        // Check the form is valid
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
                // Launch saving operations as transaction so they can be rolled back on error
                recipeRepository.withTransaction {
                    var recipeId: Int? = recipe?.recipe?.id
                    val recipeName = editRecipeState.value.title ?: "Untitled"
                    val recipeImage = editRecipeState.value.image
                    val recipeDescription = editRecipeState.value.description

                    // Save the recipe
                    val newRecipe =
                        Recipe(
                            id = recipeId,
                            name = recipeName,
                            image = recipeImage,
                            description = recipeDescription
                        )

                    recipeRepository.insertOrUpdate(newRecipe)
                    // TODO: Replace Room's upsert with custom method that returns new object id

                    if(recipeId == null)
                    {
                        recipeId = recipeRepository.getRecipeIdByName(recipeName) // TODO: Unclean
                        // TODO: this will be useless when we replace room upsert with something better
                    }

                    // Remove ingredients that the user deleted
                    val ingredientsToRemove = editRecipeState.value
                        .removedIngredients.map{ it.ingredientId ?: -1 }

                    recipeRepository.deleteIngredientsById(*ingredientsToRemove.toIntArray())

                    // Save ingredients to database
                    if(editRecipeState.value.ingredients.isNotEmpty())
                    {
                        // Make sure the highest percentage is 100%
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

                        recipeRepository.insertOrUpdateIngredients(*ingredientsToUpdate.toTypedArray())
                    }

                    // Delete timers that the user has removed
                    val stepsToRemove = editRecipeState.value
                        .removedSteps.map{ it.stepId ?: -1 }

                    stepRepository.deleteStepsById(*stepsToRemove.toIntArray())

                    // Save timers to database
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

                       stepRepository.insertOrUpdateSteps(*stepsToUpdate.toTypedArray())
                    }
                }
            }
        }
        return validForm
    }
    /*
        Adds a new ingredient field to the form
     */
    fun newIngredient()
    {
        _editRecipeState.value = _editRecipeState.value.copy(
            ingredients = (_editRecipeState.value.ingredients.toMutableList() +
                EditRecipeIngredientField()).toList()
        )
    }
    /*
        Adds a new timer field to the form
     */
    fun newStep()
    {
        _editRecipeState.value = _editRecipeState.value.copy(
            steps = (_editRecipeState.value.steps.toMutableList() +
                    EditRecipeStepField()).toList()
        )
    }
    /*
        Update the image URI on the form
     */
    fun updateImage(uri: Uri?)
    {
        if(uri != null)
        {
            _editRecipeState.value = _editRecipeState.value.copy(image = uri)
        }
    }
    /*
        Update the ingredient's properties on the form
     */
    fun updateIngredient(index: Int, new:EditRecipeIngredientField)
    {
        if(isStringAValidBigDecimalOrEmptyOrNull(new.percent)
            && (new.percent?.length ?: 0) <= 3
            && (new.name?.length ?: 0) <= 30
        )
        {
            val mutableIngredientList = _editRecipeState.value.ingredients.toMutableList()

            mutableIngredientList[index] = new

            _editRecipeState.value = _editRecipeState.value.copy(
                ingredients = mutableIngredientList.toList()
            )
        }
    }
    /*
        Update the timer's properties on the form
     */
    fun updateStep(index: Int, new:EditRecipeStepField)
    {
        if(isStringAValidBigDecimalOrEmptyOrNull(new.duration)
            && (new.duration?.length ?: 0) < 9
        )
        {
            val mutableStepList = _editRecipeState.value.steps.toMutableList()

            mutableStepList[index] = new

            _editRecipeState.value = _editRecipeState.value.copy(
                steps = mutableStepList.toList()
            )
        }
    }
    /*
        Returns true if the string is a valid big decimal, empty, or null
     */
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
    /*
        Updates the recipe's name on the form state
     */
    fun updateName(newName:String)
    {
        _editRecipeState.value = _editRecipeState.value.copy(title = newName)
    }
    /*
        Update the recipe's description on the form state
     */
    fun updateDescription(newDescription:String)
    {
        _editRecipeState.value = _editRecipeState.value.copy(description = newDescription)
    }
    /*
        Removes an ingredient from the form state
     */
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
    /*
        Removes a timer from the form state
     */
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
    /*
        Converts the Ingredient db entity to the corresponding form state
     */
    private fun ingredientsToIngredientField(ingredients: List<Ingredient>): List<EditRecipeIngredientField>
    {
        return ingredients.sortedBy{it.id}.map { ingredient ->
            EditRecipeIngredientField(
                name = ingredient.name,
                percent = ingredient.percent.toString(),
                ingredientId = ingredient.id,
            )
        }
    }
    /*
        Converts the timer db entity to the corresponding state form data
     */
    private fun stepsToStepField(steps: List<Step>): List<EditRecipeStepField>
    {
        return steps.sortedBy{it.id}.map { step ->
            EditRecipeStepField(
                description = step.description,
                duration = step.duration.roundToInt().toString(),
                stepId = step.id,
            )
        }
    }
    /*
        Gets recipe and ingredients by id
     */
    private fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients?> =
        recipeRepository.getRecipeWithIngredientsById(recipeId)
}