package com.example.bakersrecipes.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bakersrecipes.R
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import com.example.bakersrecipes.ui.theme.Typography

@Preview(showBackground = true)
@Composable
fun RecipeItemPreview() {
    BakersRecipesTheme {
        RecipeItem(Recipe(name = "Android"), { })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeItem(recipe: Recipe, onRecipeClicked: (recipeId: Int?) -> Unit, modifier: Modifier = Modifier) {
    Card (
        onClick = { onRecipeClicked(recipe.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column{
            AsyncImage(
                model = recipe.image ?: R.drawable.ic_launcher_background,
                contentDescription = recipe.name,
                modifier = Modifier
                    .height(128.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    recipe.name,
                    style = Typography.titleMedium
                )

                recipe.description?.let { description ->
                    Text(
                        description,
                        style = Typography.labelSmall
                    )
                }
            }

        }
    }
}