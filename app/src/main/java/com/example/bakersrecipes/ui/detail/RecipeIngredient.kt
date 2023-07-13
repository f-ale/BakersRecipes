package com.example.bakersrecipes.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.bakersrecipes.R
import com.example.bakersrecipes.data.datatypes.Percentage
import com.example.bakersrecipes.data.datatypes.toPercentage

@Composable
fun RecipeIngredient(
    name:String,
    number: Percentage,
    showWeight:Boolean,
    weightUnit:String,
    modifier: Modifier = Modifier)
{
    var numberString = if(showWeight) number.toUnformattedString() else number.toString()

    numberString += if(showWeight)
        weightUnit
    else
        stringResource(id = R.string.percent_unit)

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
    RecipeIngredient("test", 1.toPercentage(), false, "g")
}