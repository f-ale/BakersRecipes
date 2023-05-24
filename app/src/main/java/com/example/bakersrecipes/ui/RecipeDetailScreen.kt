package com.example.bakersrecipes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.R
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import com.example.bakersrecipes.ui.theme.Typography

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview()
{
    BakersRecipesTheme {
        RecipeDetailScreen(
            RecipeWithIngredients(
                Recipe(1,"test"),
                listOf(
                    Ingredient(1, 1, "test", 1.0f)
                )
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recipe: RecipeWithIngredients)
{
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column() {
            Image(
                painterResource(id = R.drawable.ic_launcher_background),
                "test",
                Modifier
                    .height(248.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            Text(
                recipe.recipe.name,
                style = Typography.titleLarge,
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        end = 16.dp,
                        start = 16.dp
                    )
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp)
            )
            {
                IngredientList(ingredients = recipe.ingredients)

                Card( modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth())
                {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    )
                    {
                        Text(
                            "Make Recipe",
                            style = Typography.titleMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedTextField(
                                "",
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                label = { Text("Total recipe weight...") },
                                leadingIcon = { Icon(Icons.Filled.Edit, "") },

                                )
                        }
                    }
                }
            }
        }
    }
}