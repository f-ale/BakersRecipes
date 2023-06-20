package com.example.bakersrecipes.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.R
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme

@Preview(showBackground = true)
@Composable
fun BakersRecipeHomePreview()
{
    BakersRecipesTheme {
        BakersRecipeHome(
            listOf(Recipe(name = "test")),
            {},
            {},
            {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakersRecipeHome(
    recipes: List<Recipe>,
    onAddRecipe: () -> Unit,
    onRecipeClicked: (recipeId: Int?) -> Unit,
    onSettingsButtonPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "\uD83D\uDC68\u200D\uD83C\uDF73"
                    )
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onSettingsButtonPressed) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        content = {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    bottom = 24.dp,
                    start = 24.dp,
                    end = 24.dp
                )
            ) {
                items(recipes) {
                        recipe: Recipe ->  RecipeItem(recipe, onRecipeClicked)
                }
            }
        }
    },
    floatingActionButton = {
            NewRecipeFAB(onClick = onAddRecipe)
    })
    // A surface container using the 'background' color from the theme

}

@Composable
fun NewRecipeFAB(onClick: () -> Unit, modifier: Modifier = Modifier)
{
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(Icons.Filled.Add, stringResource(id = R.string.add_recipe))
    }
}
