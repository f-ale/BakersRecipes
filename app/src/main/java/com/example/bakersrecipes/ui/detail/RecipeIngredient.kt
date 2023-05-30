package com.example.bakersrecipes.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
fun RecipeIngredient(name:String, number:Float, showWeight:Boolean, modifier: Modifier = Modifier)
{
    var numberString = number.roundToInt().toString()
    if(showWeight)
        numberString += "g"
    else
        numberString += "%"

    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            name,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            numberString,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeIngredientPreview()
{
    RecipeIngredient("test", 1f, false)
}