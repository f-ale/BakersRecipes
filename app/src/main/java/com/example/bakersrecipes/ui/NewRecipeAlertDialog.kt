package com.example.bakersrecipes.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecipeAlertDialog(modifier: Modifier = Modifier)
{
    AlertDialog(
        onDismissRequest = { /*TODO*/ },
        confirmButton = { Button(onClick = {  }) { Text("Add Recipe") }  },
        dismissButton = { Button(onClick = {  }) { Text("Cancel") } },
        title = {
            Text("New Recipe")
        },
        text = {
            OutlinedTextField(
                "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text("Recipe name...") },
                leadingIcon = { Icon(Icons.Filled.Edit, "") }
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun NewRecipeAlertDialogPreview()
{
    BakersRecipesTheme {
        NewRecipeAlertDialog()
    }
}