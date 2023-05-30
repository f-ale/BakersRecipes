package com.example.bakersrecipes.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import com.example.bakersrecipes.ui.theme.Typography

@Preview(showBackground = true)
@Composable
fun BakersRecipeHomePreview()
{
    BakersRecipesTheme {
        BakersRecipeHome(listOf(Recipe(name = "test")),{},{})
    }
}

@Composable
fun BakersRecipeHome(
    recipes: List<Recipe>,
    onAddRecipe: () -> Unit,
    onRecipeClicked: (recipeId: Int?) -> Unit,
    modifier: Modifier = Modifier
)
{
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            item {
                Text(
                    "Baker's Recipes",
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(
                        top = 16.dp,
                        end = 16.dp
                    )
                )
            }

            items(recipes) {
                    recipe: Recipe ->  RecipeItem(recipe, onRecipeClicked)
            }

            item {
                NewRecipeFAB(onClick = onAddRecipe)
            }
        }
    }
}

@Composable
fun NewRecipeFAB(onClick: () -> Unit, modifier: Modifier = Modifier)
{
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(64.dp)
    ) {
        Icon(Icons.Filled.Add, "Add Recipe")
    }
}
