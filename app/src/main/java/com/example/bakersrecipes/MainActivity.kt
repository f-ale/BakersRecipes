package com.example.bakersrecipes
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bakersrecipes.data.Recipe
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import com.example.bakersrecipes.ui.BakersRecipeHome
import com.example.bakersrecipes.ui.RecipeDetailScreen
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import dagger.hilt.android.AndroidEntryPoint

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
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    tint = Color.White, // TODO: Hardcoded color?
                    contentDescription = "back button"
                )
            }
        }
    }
}




