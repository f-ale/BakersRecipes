package com.example.bakersrecipes.ui.edit

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
        if (recipeId != null) {
            viewModelScope.launch {
                getRecipeWithIngredientsById(recipeId).collect { it ->
                    if(it != null) {
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
            }

        if(validForm) {
            viewModelScope.launch {
                db.withTransaction {
                    var recipeId: Int? = recipe?.recipe?.id
                    val recipeName = editRecipeState.value.title ?: "Untitled"

                    val newRecipe =
                        Recipe(
                            recipeId,
                            recipeName
                        )

                    db.recipeDao().insertOrUpdate(newRecipe)
                    // TODO: Replace Room's upsert with custom method that returns new object id

                    if(recipeId == null)
                    {
                        recipeId = db.recipeDao().getRecipeIdByName(recipeName)
                        // TODO: this will be useless when we replace room upsert with something better
                    }

                    if(editRecipeState.value.ingredients.isNotEmpty())
                    {
                        val ingredientDao = db.ingredientDao()
                        val ingredientsToRemove = editRecipeState.value
                            .removedIngredients.map{ it.ingredientId ?: -1 }

                        ingredientDao.deleteIngredientsById(*ingredientsToRemove.toIntArray())

                        val maxPercent = editRecipeState.value.ingredients
                            .maxOf{ it.percent?.toFloat() ?: 0f }

                        val ingredientsToUpdate = editRecipeState.value.ingredients.map {
                                field ->
                            Ingredient(
                                id = field.ingredientId,
                                recipeId = recipeId,
                                name = field.name ?: "Untitled",
                                percent = field.percent?.toFloatOrNull()?.div(maxPercent) ?: 0f,
                            )
                        }

                        ingredientDao.insertOrUpdateIngredients(*ingredientsToUpdate.toTypedArray())
                    }
                }
            }
        }

        // TODO: Save recipe description

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
        if(isStringAValidFloatOrEmptyOrNull(new.percent))
        {
            val mutableIngredientList = _editRecipeState.value.ingredients.toMutableList()

            mutableIngredientList[index] = new

            _editRecipeState.value = _editRecipeState.value.copy(
                ingredients = mutableIngredientList.toList()
            )
        }
    }
    private fun isStringAValidFloatOrEmptyOrNull(number: String?): Boolean
    {
        if(number == null || number == "")
            return true

        return try {
            number.toFloat().toString()
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

    private fun ingredientsToIngredientField(ingredients: List<Ingredient>): List<EditRecipeIngredientField>
    {
        return ingredients.sortedBy{it.id}.map {
            ingredient ->
            EditRecipeIngredientField(
                name = ingredient.name,
                percent = ingredient.percent.times(100).toString(),
                ingredientId = ingredient.id,
            )
        }
    }

    private fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients?> =
        db.recipeDao().getRecipeWithIngredientsById(recipeId)
}