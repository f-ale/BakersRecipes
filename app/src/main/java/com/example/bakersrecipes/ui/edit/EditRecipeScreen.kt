package com.example.bakersrecipes.ui.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/*@Preview
@Composable
fun EditRecipeScreenPreview()
{
    BakersRecipesTheme {
        EditRecipeScreen()
    }
}*/

@Composable
fun EditRecipeScreen( // TODO: Make scrollable
    viewModel: EditRecipeViewModel,
    onSaveEdits: () -> Unit,
    modifier: Modifier = Modifier)
{
    val editRecipeState: EditRecipeState by viewModel.editRecipeState.collectAsState()

    Surface(modifier = modifier.fillMaxSize()) {
        Column {
            TextEditField(
                editRecipeState.title ?: "",
                "Title",
                Icons.Outlined.Info,
                onValueChange = { viewModel.updateName(it) }
            )
            TextEditField(
                editRecipeState.description ?: "",
                "Description",
                Icons.Outlined.Info,
                onValueChange = { viewModel.updateDescription(it) }
            )
            EditableIngredientList(
                ingredients = editRecipeState.ingredients,
                onEditFieldValueChange = { viewModel.updateIngredient(it) }
            )

            Button(onClick = { // TODO: Only save if fields are valid
                viewModel.saveChanges() // return false if fields are invalid
                onSaveEdits()
            }) {
                Text("Save")
            }

            Button(onClick = { viewModel.newIngredient() }) {
                Text("New Ingredient")
            }
        }
    }
}

@Composable
fun EditableIngredientList(
    ingredients: List<EditRecipeIngredientField>,
    modifier: Modifier = Modifier,
    onEditFieldValueChange: (EditRecipeIngredientField) -> Unit,
) {
    Column(modifier = modifier) {
        ingredients.forEach {
            ingredient ->
            IngredientEditField(
                name = ingredient.name ?: "",
                percent = ingredient.percent?.times(100)?.roundToInt() ?: 0,
                Icons.Outlined.Edit,
                { onEditFieldValueChange(
                    ingredient.copy(
                        name = it.first ?: ingredient.name,
                        percent = it.second?.toFloatOrNull()?.div(100) ?: ingredient.percent
                    )) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditField(
    name: String,
    percent:Int,
    icon: ImageVector,
    onValueChange: (Pair<String?, String?>) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Warn if percentage = 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            icon,
            name,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(32.dp)
                .offset(y = 3.dp)
        )
        OutlinedTextField(
            name,
            onValueChange = { onValueChange(Pair(it, null)) },
            label = { Text("Ingredient") },
            modifier = Modifier.width(154.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            percent.toString(),
            onValueChange = { onValueChange(Pair(null, it)) },
            label = { Text("Percentage") },
            trailingIcon = { Text("%") },
            modifier = Modifier.width(154.dp),
            keyboardOptions = KeyboardOptions.Default
                .copy(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.width(32.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditField(
    name: String,
    label: String,
    icon: ImageVector,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            icon,
            name,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(32.dp)
                .offset(y = 3.dp)
        )
        OutlinedTextField(
            name,
            onValueChange = onValueChange,
            label = { Text(label) }
        )
        Spacer(modifier = Modifier.width(32.dp))
    }
}
