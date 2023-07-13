package com.example.bakersrecipes.ui.home

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bakersrecipes.data.RecipeDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val db:RecipeDatabase,
    val dataStore: DataStore<Preferences>
): ViewModel()
{
    private val searchStringState: MutableStateFlow<String?> = MutableStateFlow(null)
    private val recipes: Flow<List<RecipeItemState>> = getAllRecipes()
    val homeScreenState: StateFlow<HomeScreenState> = searchStringState.combine(recipes) {
        searchString, recipes ->
        HomeScreenState(
            recipes = recipes.filter { recipe ->
                if(!searchString.isNullOrEmpty()) {
                    recipe.name.lowercase().contains(searchString.lowercase())
                }
                else
                {
                    true
                }
            },
            searchString = searchString
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeScreenState())
    fun updateSearchString(searchString: String?)
    {
        searchStringState.value = if(searchString.isNullOrEmpty()) null else searchString
    }
    private fun getAllRecipes(): Flow<List<RecipeItemState>> =
        db.recipeDao().getAllRecipes().map { recipeList ->
            recipeList.map { recipe ->
                RecipeItemState(
                    id = recipe.id ?: -1,
                    name = recipe.name,
                    description = recipe.description ?: "",
                    image = recipe.image
                )
            }
        }
}
data class RecipeItemState (
    val name:String = "",
    val description: String = "",
    val id:Int = -1,
    val image: Uri? = null,
    )

data class HomeScreenState (
    val recipes: List<RecipeItemState> = listOf(),
    val searchString: String? = null,
)