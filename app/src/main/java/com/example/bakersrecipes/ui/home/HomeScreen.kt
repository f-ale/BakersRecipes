package com.example.bakersrecipes.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bakersrecipes.R
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme

@Preview(showBackground = true)
@Composable
fun BakersRecipeHomePreview()
{
    BakersRecipesTheme {
       /* BakersRecipeHome(
            listOf(Recipe(name = "test")),
            {},
            {},
            {}
        )*/
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakersRecipeHome(
    viewModel: HomeViewModel,
    onAddRecipe: () -> Unit,
    onRecipeClicked: (recipeId: Int?) -> Unit,
    onSettingsButtonPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.homeScreenState.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(id = R.drawable.bakers_recipes_logo),
                            "Baker's recipes",
                            modifier = Modifier.size(46.dp)
                                .background(color = Color(color = 0xffc40000),
                                    shape = CircleShape).padding(8.dp)
                        )
                    }

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
            if(!(state.recipes.isEmpty() && state.searchString.isNullOrEmpty()))
            {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        bottom = 24.dp,
                        start = 24.dp,
                        end = 24.dp
                    )
                ) {
                    item {
                        HomeSearchBar(
                            searchString = state.searchString,
                            updateSearchString = { searchString ->
                                viewModel.updateSearchString(searchString)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if(state.recipes.isNotEmpty()) {
                        items(state.recipes) { recipe: RecipeItemState ->
                            RecipeItem(recipe, onRecipeClicked)
                        }
                    } else {
                        item {
                            Text(
                                text = "No recipes found",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("You haven't added any recipes yet")
                }
            }
        }
    },
    floatingActionButton = {
            NewRecipeFAB(onClick = onAddRecipe)
    })
    // A surface container using the 'background' color from the theme
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    searchString: String?,
    updateSearchString: (String?) -> Unit,
    modifier: Modifier = Modifier
)
{
    SearchBar(
        query = searchString ?: "",
        onQueryChange = {
                string -> updateSearchString(string)
        },
        onSearch = {},
        active = false,
        onActiveChange = {},
        leadingIcon = {
            Icon(Icons.Default.Search, "Search")
        },
        trailingIcon = {
            if(!searchString.isNullOrEmpty()) {
                IconButton(onClick = { updateSearchString(null) }) {
                    Icon(Icons.Default.Clear, "Cancel search")
                }
            }
        },
        placeholder = { Text("Search") },
        modifier = modifier
    ) {}
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
