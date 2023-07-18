package com.example.bakersrecipes
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bakersrecipes.ui.detail.DetailViewModel
import com.example.bakersrecipes.ui.detail.RecipeDetailScreen
import com.example.bakersrecipes.ui.edit.EditRecipeScreen
import com.example.bakersrecipes.ui.edit.EditRecipeViewModel
import com.example.bakersrecipes.ui.home.BakersRecipeHome
import com.example.bakersrecipes.ui.home.HomeViewModel
import com.example.bakersrecipes.ui.settings.SettingsScreen
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import dagger.hilt.android.AndroidEntryPoint

/*
TODO: Animations
TODO: Pretty theme
TODO: Focus and scroll
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Ask contextually
        val REQUEST_CODE_PERMISSION = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the missing permission
            ActivityCompat.requestPermissions(
                this as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_PERMISSION
            )
        }

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
fun BakersRecipeApp(viewModel: HomeViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    Box {
        NavHost(
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
                    onAddRecipe = {
                        navController.navigate(BakersRecipesDestinations.New.name)
                    },
                    onRecipeClicked = { recipeId ->
                        navController.navigate(
                            BakersRecipesDestinations.Detail.name + "/$recipeId"
                        )
                    },
                    onSettingsButtonPressed = {
                        navController.navigate(BakersRecipesDestinations.Settings.name)
                    },
                    viewModel = viewModel
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

            composable(
                BakersRecipesDestinations.Detail.name + "/{recipeId}",
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
                    onEditRecipe = { recipeId ->
                        navController.navigate(BakersRecipesDestinations.Edit.name + "/$recipeId")
                    }
                )
            }

            composable(
                BakersRecipesDestinations.Edit.name + "/{recipeId}",
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




