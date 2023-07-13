package com.example.bakersrecipes.data

import android.net.Uri
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.bakersrecipes.data.datatypes.Percentage
import java.math.BigDecimal

@Database(entities = [Recipe::class, Ingredient::class, Step::class], version = 7)
@TypeConverters(Converters::class)
abstract class RecipeDatabase: RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun stepDao(): StepDao
}

class Converters {
    @TypeConverter
    fun uriFromString(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }
    @TypeConverter
    fun uriToString(uri: Uri?): String? {
        return uri?.toString()
    }
    @TypeConverter
    fun fromPercentage(percentage: Percentage): String {
        return percentage.toBigDecimal().toPlainString()
    }
    @TypeConverter
    fun toPercentage(value: String): Percentage {
        return Percentage(BigDecimal(value))
    }
}