package com.example.bakersrecipes.ui.edit

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
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

    fun saveChanges(): Boolean
    {
        val validForm = editRecipeState.value.ingredients.all {
            it.percent != null && it.name != null
        }

        if(validForm) {
            viewModelScope.launch {
                db.withTransaction {
                    val recipeId: Int? = recipe?.recipe?.id

                    val newRecipe =
                        Recipe(
                            recipeId,
                            editRecipeState.value.title ?: "Untitled"
                        )

                    db.recipeDao().insertOrUpdate(newRecipe)

                    val ingredientDao = db.ingredientDao()
                    val ingredientsToRemove = editRecipeState.value
                        .removedIngredients.map{ it.ingredientId ?: -1 }

                    ingredientDao.deleteIngredientsById(*ingredientsToRemove.toIntArray())

                    val ingredientsToUpdate = editRecipeState.value.ingredients.map {
                            field ->
                        Ingredient(
                            id = field.ingredientId,
                            recipeId = recipeId ?: 1, // TODO: No, get the new id from query
                            name = field.name ?: "Untitled",
                            percent = field.percent ?: 0f,
                        )
                    }

                    Log.d("BAKERSW", ingredientsToUpdate.joinToString(","))

                    // TODO: Check if ingredients are ok

                    ingredientDao.insertOrUpdateIngredients(*ingredientsToUpdate.toTypedArray())
                }
            }
        }

        // TODO: Save recipe description

        // TODO: Handle new recipe
        return validForm
    }

    fun newIngredient()
    {
        _editRecipeState.value = _editRecipeState.value.copy(
            ingredients = (_editRecipeState.value.ingredients.toMutableList() +
                EditRecipeIngredientField()).toList()
        )
    }

    fun updateIngredient(index: Int, new:EditRecipeIngredientField)
    {
        val mutableIngredientList = _editRecipeState.value.ingredients.toMutableList()

        mutableIngredientList[index] = new

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