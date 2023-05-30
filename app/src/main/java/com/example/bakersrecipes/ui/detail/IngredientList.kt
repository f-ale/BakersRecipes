package com.example.bakersrecipes.ui.detail

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.ui.theme.Typography

@Preview
@Composable
fun IngredientListPreview()
{
    IngredientList(listOf(Pair("",1f)), false)
}
@Composable
fun IngredientList(ingredients: List<Pair<String, Float>>, showWeight: Boolean)
{
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp))
    {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Ingredients",
                style = Typography.titleMedium
            )

            if(ingredients.isNotEmpty()) {
                LazyColumn {
                    items(ingredients) {
                            ingredient: Pair<String,Float> ->
                        RecipeIngredient(
                            name = ingredient.first,
                            number = if(showWeight) {ingredient.second} else {
                              ingredient.second * 100
                            },
                            modifier = Modifier.padding(horizontal = 16.dp),
                            showWeight = showWeight
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