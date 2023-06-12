package com.example.bakersrecipes
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bakersrecipes.ui.detail.DetailViewModel
import com.example.bakersrecipes.ui.detail.RecipeDetailScreen
import com.example.bakersrecipes.ui.edit.EditRecipeScreen
import com.example.bakersrecipes.ui.edit.EditRecipeViewModel
import com.example.bakersrecipes.ui.home.BakersRecipeHome
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
    Edit,
    New
}
@Composable
fun BakersRecipeApp(viewModel: RecipeViewModel = viewModel())
{
    val navController = rememberNavController()
    val recipes by viewModel.getAllRecipes().collectAsState(initial = emptyList()) // TODO: Move to bakersrecipehome

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
                            navController.navigate(BakersRecipesDestinations.New.name)
                    },
                    onRecipeClicked = {
                            recipeId -> navController.navigate(
                        BakersRecipesDestinations.Detail.name+"/$recipeId")
                    }
                )
            }

            // TODO: Navigate to detail
            composable(
                BakersRecipesDestinations.Detail.name+"/{recipeId}",
                arguments = listOf(
                    navArgument("recipeId") {
                        type = NavType.IntType
                    }
                )
            ) {
                val detailViewModel: DetailViewModel = hiltViewModel()
                RecipeDetailScreen(
                    detailViewModel,
                    onNavigateUp = { navController.navigateUp() },
                    onEditRecipe = {
                            recipeId -> navController.navigate(BakersRecipesDestinations.Edit.name+"/$recipeId")
                    }
                )
            }

            composable(
                BakersRecipesDestinations.Edit.name+"/{recipeId}",
                arguments = listOf(
                    navArgument("recipeId") {
                        type = NavType.IntType
                    }
                )
            ) {
                val editRecipeViewModel: EditRecipeViewModel = hiltViewModel()

                EditRecipeScreen(
                    editRecipeViewModel,
                    { navController.navigateUp() },
                    { navController.navigateUp() },
                    onRecipeDelete = {
                        navController.navigate(BakersRecipesDestinations.Home.name)
                    }
                )
            }

            composable(
                BakersRecipesDestinations.New.name
            ) {
                val editRecipeViewModel: EditRecipeViewModel = hiltViewModel()

                EditRecipeScreen(
                    editRecipeViewModel,
                    { navController.navigateUp() },
                    { navController.navigateUp() },
                    onRecipeDelete = {
                        navController.navigate(BakersRecipesDestinations.Home.name)
                    } // TODO: avoid repeated code
                )
            }
        }
    }
}




