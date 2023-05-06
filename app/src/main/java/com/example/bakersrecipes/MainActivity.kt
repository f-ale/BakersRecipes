package com.example.bakersrecipes

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.bakersrecipes.data.Ingredient
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.RecipeDatabase
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import com.example.bakersrecipes.ui.theme.Typography
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BakersRecipesTheme {
                BakersRecipeApp()
            }
        }
    }
}

enum class BakersRecipesDestinations()
{
    Home,
    Detail,
    Edit
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakersRecipeApp(viewModel: RecipeViewModel = viewModel())
{
    val navController = rememberNavController()
    val recipes by viewModel.getAllRecipes().collectAsState(initial = emptyList())

    val backStackEntry by navController.currentBackStackEntryAsState()

    Box {
        NavHost(
            navController = navController,
            startDestination = BakersRecipesDestinations.Home.name,
        ) {
            composable(BakersRecipesDestinations.Home.name) {
                BakersRecipeHome(
                    recipes = recipes,
                    onAddRecipe = {
                        viewModel.insertRecipe(Recipe(name = "test"))
                    },
                    onRecipeClicked = {
                            recipeId -> navController.navigate(BakersRecipesDestinations.Detail.name+"/$recipeId")
                    }
                )
            }

            // TODO: Navigate to detail
            composable(
                BakersRecipesDestinations.Detail.name+"/{recipeId}",
                arguments =
                listOf(
                    navArgument("recipeId") {
                        type = NavType.IntType
                    }
                )
            ) { backstackEntry ->
                val recipeId = backstackEntry.arguments?.getInt("recipeId") ?: -1
                val recipe: RecipeWithIngredients
                        by viewModel.getRecipeWithIngredientsById(recipeId).collectAsState(
                            initial = RecipeWithIngredients(
                                Recipe(-1,"null"),
                                listOf()
                            )
                        )
                RecipeDetailScreen(recipe)
            }
        }

        if((backStackEntry?.destination?.displayName ?: "Home") != BakersRecipesDestinations.Home.name
            && (navController.previousBackStackEntry != null))
        {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "back button"
                        )
                    }
                })
        }
    }
}

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
                IngredientsList(ingredients = recipe.ingredients)

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
                                label = { Text("Total recipe weight...")},
                                leadingIcon = { Icon(Icons.Filled.Edit, "")}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientsList(ingredients: List<Ingredient>)
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

        }
    }
}

@Composable
fun RecipeIngredient(name:String, percent:Float, modifier: Modifier = Modifier)
{
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            name,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            percent.toString(),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

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
        modifier = Modifier.fillMaxSize(),
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
                recipe: Recipe ->  RecipeItem(recipe, onRecipeClicked);
            }

            item {
                NewRecipeFAB(onClick = onAddRecipe)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecipeAlertDialog(modifier: Modifier = Modifier)
{
    AlertDialog(
        onDismissRequest = { /*TODO*/ },
        confirmButton = { Button(onClick = {  }) { Text("Add Recipe") }  },
        dismissButton = { Button(onClick = {  }) { Text("Cancel") }},
        title = {
                Text("New Recipe")
        },
        text = {
            OutlinedTextField(
                "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text("Recipe name...")},
                leadingIcon = { Icon(Icons.Filled.Edit, "")}
            )
        }
    )
}

@Preview
@Composable
fun NewRecipeAlertDialogPreview()
{
    BakersRecipesTheme {
        NewRecipeAlertDialog()
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

@Composable
fun RecipeItem(recipe:Recipe, onRecipeClicked: (recipeId: Int?) -> Unit, modifier: Modifier = Modifier) {
    Card (
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRecipeClicked(recipe.id) }
    ) {
        Column(
        ) {
            Image(
                painterResource(id = R.drawable.ic_launcher_background),
                recipe.name,
                Modifier
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
                Text(
                    "by testuser", // TODO: Change
                    style = Typography.labelSmall
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeItemPreview() {
    BakersRecipesTheme {
        RecipeItem(Recipe(name = "Android"), { })
    }
}