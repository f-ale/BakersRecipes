package com.example.bakersrecipes.data

import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.bakersrecipes.data.relations.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

@Entity(tableName="recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
    val image: Uri? = null,
    val description: String? = null,
    val name:String,
)
@Dao
interface RecipeDao {
    @Insert
    suspend fun insert(recipe: Recipe)
    @Upsert
    suspend fun insertOrUpdate(recipe: Recipe): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(vararg recipes: Recipe)
    @Delete
    suspend fun delete(recipe: Recipe)
    @Query("SELECT id FROM recipes WHERE name = :name ORDER BY id DESC LIMIT 1")
    fun getRecipeIdByName(name: String): Int
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<Recipe>>
    @Transaction
    @Query("SELECT * FROM recipes")
    fun getAllRecipesWithIngredients():List<RecipeWithIngredients>
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    fun getRecipeWithIngredientsById(recipeId: Int): Flow<RecipeWithIngredients?>
}
