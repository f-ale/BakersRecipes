package com.example.bakersrecipes.ui.detail

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.R
import com.example.bakersrecipes.data.datatypes.Percentage
import com.example.bakersrecipes.ui.theme.Typography

fun LazyListScope.ingredientList(
    ingredients: List<Pair<String, Percentage>>,
    showWeight: Boolean,
    weightUnit: String,
    paddingTop: Dp = 0.dp,
    paddingBottom: Dp = 0.dp,
    paddingHorizontal: Dp = 0.dp,
) {
    item {
        Spacer(modifier = Modifier.height(paddingTop))
    }
    item {
        Text(
            stringResource(id = R.string.ingredients),
            style = Typography.titleMedium,
            modifier = Modifier.padding(horizontal = paddingHorizontal)
        )
    }
    item {
        Spacer(modifier = Modifier.height(6.dp))
    }
    if(ingredients.isNotEmpty()) {
        items(ingredients) {
                ingredient: Pair<String, Percentage> ->
            RecipeIngredient(
                name = ingredient.first,
                number = ingredient.second,
                modifier = Modifier.padding(
                    horizontal = paddingHorizontal + 16.dp,
                    vertical = 2.dp
                ),
                showWeight = showWeight,
                weightUnit = weightUnit,
            )
        }
    } else {
        item {
            Text(
                stringResource(id = R.string.none_yet),
                modifier = Modifier
                    .fillMaxWidth().padding(paddingHorizontal),
                textAlign = TextAlign.Center,
                style = Typography.bodyMedium
            )
        }
    }

    item {
        Spacer(modifier = Modifier.height(paddingBottom))
    }
}