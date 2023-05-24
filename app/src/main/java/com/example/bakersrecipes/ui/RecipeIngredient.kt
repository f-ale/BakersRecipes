package com.example.bakersrecipes.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RecipeIngredient(name:String, percent:Float, modifier: Modifier = Modifier)
{
    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            name,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            percent.toString(),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}