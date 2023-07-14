package com.example.bakersrecipes.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(index = true)
    val stepId: Int,
    @ColumnInfo(index = true)
    val recipeId: Int,
    val scheduledTime: Long = -1
)

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(alarm: Alarm)

    @Query("SELECT * FROM Alarm WHERE stepId = :stepId AND recipeId = :recipeId")
    suspend fun getAlarm(stepId: Int, recipeId: Int): Alarm?

    @Query("SELECT * FROM Alarm")
    suspend fun getAllAlarms(): List<Alarm>

    @Query("DELETE FROM Alarm WHERE stepId = :stepId AND recipeId = :recipeId")
    suspend fun delete(stepId: Int, recipeId: Int)
}