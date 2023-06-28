package com.example.bakersrecipes.data

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
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = CASCADE
        )]
)
data class Step(
    @PrimaryKey(autoGenerate = true)
    val id:Int?,
    @ColumnInfo(index = true)
    val recipeId:Int,
    val description:String,
    val duration:Float,
)

@Dao
interface StepDao {
    @Insert
    fun insert(step: Step)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(vararg steps: Step)
    @Upsert
    suspend fun insertOrUpdateSteps(vararg steps: Step)
    @Delete
    suspend fun delete(step: Step)
    @Delete
    suspend fun deleteSteps(vararg steps: Step)
    @Query("DELETE FROM steps WHERE id IN (:stepId)")
    suspend fun deleteStepsById(vararg stepId:Int)
    @Query("SELECT * FROM steps")
    fun getAllSteps(): List<Step>
    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY id ASC")
    fun getStepsForRecipe(recipeId: Int): Flow<List<Step>>
}