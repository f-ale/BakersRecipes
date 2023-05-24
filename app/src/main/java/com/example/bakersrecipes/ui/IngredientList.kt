package com.example.bakersrecipes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.ui.theme.Typography


@Composable
fun IngredientList(ingredients: List<Ingredient>)
{
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp))
    {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            Text(
                "Ingredients",
                style = Typography.titleMedium
            )

            if(ingredients.isNotEmpty()) {
                LazyColumn {
                    items(ingredients) {
                            ingredient: Ingredient ->
                        RecipeIngredient(
                            name = ingredient.name,
                            percent = ingredient.percent,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                Text(
                    "None yet",
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = Typography.bodyMedium
                )
            }

        }
    }
}