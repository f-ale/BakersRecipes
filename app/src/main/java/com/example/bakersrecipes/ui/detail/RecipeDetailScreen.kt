package com.example.bakersrecipes.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bakersrecipes.R
import com.example.bakersrecipes.ui.common.BackButton

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview()
{
    /*
    BakersRecipesTheme {
        RecipeDetailScreen(DetailViewModel())
    }*/
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen( // TODO: Make scrollable
    viewModel: DetailViewModel,
    onNavigateUp: () -> Unit,
    onEditRecipe: (Int) -> Unit
) {
    val recipeDetailState by viewModel.recipeDetailState.collectAsState()

    // TODO: Delete recipe

    // A surface container using the 'background' color from the theme
    Scaffold(
        topBar = {
                 MediumTopAppBar(
                     title = {
                         Text(recipeDetailState.recipe?.name ?: "")
                     },
                     navigationIcon = {
                        BackButton(onClick = onNavigateUp)
                 }, actions = {
                     IconButton(
                         onClick = { onEditRecipe(viewModel.recipeId) },
                         content = { Icon(Icons.Outlined.Edit, "Edit") })
                     })
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                AsyncImage(
                    model = recipeDetailState.recipe?.image ?: R.drawable.ic_launcher_background,
                    recipeDetailState.recipe?.name ?: "Thumbnail",
                    Modifier
                        .height(248.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    recipeDetailState.recipe?.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IngredientList(ingredients = recipeDetailState.ingredientDisplayList,
                        recipeDetailState.totalRecipeWeight != null)

                    if(recipeDetailState.ingredientDisplayList.isNotEmpty())
                    {
                        Divider()
                        MakeRecipeForm(
                            recipeDetailState.totalRecipeWeight?.toString() ?: "",
                            onUpdateTotalRecipeWeight =
                            { viewModel.updateMakeRecipeWeightFromString(it) }
                        )
                    }
                }
            }
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeRecipeForm(
    totalRecipeWeight: String,
    onUpdateTotalRecipeWeight: (String) -> Unit
) {
    Column {
        Text(
            stringResource(id = R.string.make_recipe),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                totalRecipeWeight,
                onValueChange = onUpdateTotalRecipeWeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text(stringResource(id = R.string.total_recipe_weight)) },
                trailingIcon = { Text("g") },
                keyboardOptions = KeyboardOptions
                    .Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
}