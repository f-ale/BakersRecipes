package com.example.bakersrecipes
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.bakersrecipes.ui.detail.DetailViewModel
import com.example.bakersrecipes.ui.detail.RecipeDetailScreen
import com.example.bakersrecipes.ui.edit.EditRecipeScreen
import com.example.bakersrecipes.ui.edit.EditRecipeViewModel
import com.example.bakersrecipes.ui.home.BakersRecipeHome
import com.example.bakersrecipes.ui.settings.SettingsScreen
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
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
    New,
    Settings
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BakersRecipeApp(viewModel: RecipeViewModel = viewModel())
{
    val navController = rememberAnimatedNavController()
    val recipes by viewModel.getAllRecipes().collectAsState(initial = emptyList()) // TODO: Move to bakersrecipehome

    Box {
        AnimatedNavHost(
            navController = navController,
            startDestination = BakersRecipesDestinations.Home.name,
        ) {
            composable(
                BakersRecipesDestinations.Home.name,
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(700)
                    )
                }
            ) {
                BakersRecipeHome(
                    recipes = recipes,
                    onAddRecipe = {
                            navController.navigate(BakersRecipesDestinations.New.name)
                    },
                    onRecipeClicked = {
                            recipeId -> navController.navigate(
                        BakersRecipesDestinations.Detail.name+"/$recipeId")
                    },
                    onSettingsButtonPressed = {
                        navController.navigate(BakersRecipesDestinations.Settings.name)
                    }
                )
            }

            composable(BakersRecipesDestinations.Settings.name) {
                SettingsScreen(
                    dataStore = viewModel.dataStore,
                    onBackPressed = {
                        navController.navigateUp()
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
                ),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(700)
                    )
                },
                exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )
                }
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




