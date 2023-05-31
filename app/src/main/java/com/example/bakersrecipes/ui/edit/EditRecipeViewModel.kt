package com.example.bakersrecipes.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val db:RecipeDatabase,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val recipeId: Int? = savedStateHandle.get<Int>("recipeId")
    private val _editRecipeState = MutableStateFlow(EditRecipeState())
    private var recipe: RecipeWithIngredients? = null

    val editRecipeState: StateFlow<EditRecipeState> = _editRecipeState.asStateFlow()

    init {
        if (recipeId == null) {
            // TODO: New recipe
        } else {
            viewModelScope.launch {
                getRecipeWithIngredientsById(recipeId).collect { it ->
                    recipe = it
                    _editRecipeState.value =
                        EditRecipeState(
                            title = it.recipe.name,
                            description = it.recipe.name,
                            ingredients = ingredientsToIngredientField(it.ingredients)
                                .sortedByDescending { it.percent }
                        )
                }
            }

            // TODO: Edit recipe
        }
    }

    fun saveChanges() // TODO: :Boolean
    {
        viewModelScope.launch {
            val recipeId: Int? = recipe?.recipe?.id

            val newRecipe =
                Recipe(
                    recipeId,
                    editRecipeState.value.title ?: "Untitled"
                )

            db.recipeDao().insertOrUpdate(newRecipe)

            val ingredientsToUpdate = editRecipeState.value.ingredients.map {
                field ->
                Ingredient(
                    id = field.ingredientId,
                    recipeId = recipeId ?: 1, // TODO: No, get the new id from query
                    name = field.name ?: "Untitled",
                    percent = field.percent ?: 0f,
                )
            }

            // TODO: Check if ingredients are ok

            db.ingredientDao().insertOrUpdateIngredients(*ingredientsToUpdate.toTypedArray())
        }

        // TODO: Save recipe description

        // TODO: Remove removed ingredients

        // TODO: Handle new recipe

        // TODO: Check if fields are valid before saving
    }

    fun newIngredient()
    {
        _editRecipeState.value = _editRecipeState.value.copy(
            ingredients = (_editRecipeState.value.ingredients.toMutableList() +
                EditRecipeIngredientField()).toList()
        )
    }

    fun updateIngredient(editRecipeIngredientField: EditRecipeIngredientField)
    {
        val mutableIngredientList = _editRecipeState.value.ingredients.toMutableList()

        mutableIngredientList[mutableIngredientList.indexOfFirst
        { it.ingredientId == editRecipeIngredientField.ingredientId }] =
            EditRecipeIngredientField(
                ingredientId = editRecipeIngredientField.ingredientId,
                name = editRecipeIngredientField.name,
                percent = editRecipeIngredientField.percent
            )

        _editRecipeState.value = _editRecipeState.value.copy(
            ingredients = mutableIngredientList.toList()

        )
    }

    fun updateName(newName:String)
    {
        _editRecipeState.value = _editRecipeState.value.copy(title = newName)
    }

    fun updateDescription(newDescription:String)
    {
        _editRecipeState.value = _editRecipeState.value.copy(description = newDescription)
    }

    fun removeIngredient(fieldIndex:Int)
    {
        val mutableIngredientList = _editRecipeState.value.ingredients.toMutableList()
        val removedIngredient = mutableIngredientList[fieldIndex]
        mutableIngredientList.removeAt(fieldIndex)

        val mutableRemovedIngredientList = _editRecipeState.value.removedIngredients.toMutableList()
        mutableRemovedIngredientList.add(removedIngredient)

        _editRecipeState.value = _editRecipeState.value.copy(
            ingredients = mutableIngredientList,
            removedIngredients = mutableRemovedIngredientList
        )
    }

    private fun ingredientsToIngredientField(ingredients: List<Ingredient>): List<EditRecipeIngredientField>
    {
        return ingredients.sortedBy{it.id}.map {
            ingredient ->
            EditRecipeIngredientField(
                name = ingredient.name,
                percent = ingredient.percent,
                ingredientId = ingredient.id,
            )
        }
    }

    private fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients> =
        db.recipeDao().getRecipeWithIngredientsById(recipeId)
}