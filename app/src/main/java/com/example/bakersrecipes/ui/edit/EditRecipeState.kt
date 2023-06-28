package com.example.bakersrecipes.ui.edit
import android.net.Uri
data class EditRecipeState (
    val title:String? = null,
    val description:String? = null,
    val image:Uri? = null,
    val ingredients:List<EditRecipeIngredientField> = listOf(),
    val removedIngredients: List<EditRecipeIngredientField> = listOf(),
    val steps:List<EditRecipeStepField> = listOf(),
    val removedSteps: List<EditRecipeStepField> = listOf(),
)
data class EditRecipeIngredientField(
    val ingredientId:Int? = null,
    val name:String? = null,
    val percent:String? = null,
)
data class EditRecipeStepField(
    val stepId:Int? = null,
    val description:String? = null,
    val duration:String? = null,
)