package com.example.bakersrecipes.ui.edit

data class EditRecipeState (
    val title:String? = null,
    val description:String? = null,
    val ingredients:List<EditRecipeIngredientField> = listOf(),
    val removedIngredients: List<EditRecipeIngredientField> = listOf()
)

data class EditRecipeIngredientField(
    val ingredientId:Int? = null,
    val name:String? = null,
    val percent:String? = null,
)