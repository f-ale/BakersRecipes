package com.example.bakersrecipes.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.Recipe

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<Ingredient>
)
