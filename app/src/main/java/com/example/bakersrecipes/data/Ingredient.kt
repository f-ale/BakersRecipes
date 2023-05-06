package com.example.bakersrecipes.data

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = CASCADE
        )]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    @ColumnInfo(index = true)
    val recipeId:Int,
    val name:String,
    val percent:Float,
)

@Dao
interface IngredientDao {
    @Insert
    fun insert(ingredient: Ingredient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(vararg ingredients: Ingredient)

    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): List<Ingredient>


    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY percent DESC")
    fun getIngredientsForRecipe(recipeId: Int): Flow<List<Ingredient>>
}