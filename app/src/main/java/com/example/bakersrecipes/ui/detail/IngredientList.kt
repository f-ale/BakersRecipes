package com.example.bakersrecipes.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.R
import com.example.bakersrecipes.ui.theme.Typography

@Preview
@Composable
fun IngredientListPreview()
{
    IngredientList(listOf(Pair("",1f)), false, "g")
}
@Composable
fun IngredientList(ingredients: List<Pair<String, Float>>, showWeight: Boolean, weightUnit: String)
{
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            stringResource(id = R.string.ingredients),
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
                        showWeight = showWeight,
                        weightUnit = weightUnit
                    )
                }
            }
        } else {
            Text(
                stringResource(id = R.string.none_yet),
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = Typography.bodyMedium
            )
        }
    }
}