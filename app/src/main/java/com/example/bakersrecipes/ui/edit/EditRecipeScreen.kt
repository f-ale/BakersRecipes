package com.example.bakersrecipes.ui.edit

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.bakersrecipes.R
import com.example.bakersrecipes.ui.common.BackButton
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    viewModel: EditRecipeViewModel,
    onSaveEdits: () -> Unit,
    onNavigateUp: () -> Unit,
    onRecipeDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editRecipeState: EditRecipeState by viewModel.editRecipeState.collectAsState()
    var overflowMenuExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle("g")
    val showWeight by viewModel.showWeight.collectAsStateWithLifecycle(false)

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        viewModel.updateImage(uri)
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, flag)
        }
    }

    val listState = rememberLazyListState()

    //TODO: Handle scaffold state
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar({ Text(stringResource(id = R.string.edit_recipe)) },
                actions = {
                    Button(onClick = {
                        val valid = viewModel.saveChanges()
                        if (valid) onSaveEdits()
                        // TODO: Show warning if conditions to save not met
                    }) {
                        Text("Save")
                    }

                    IconButton(onClick = {
                        overflowMenuExpanded = !overflowMenuExpanded
                    }) {
                        Icon(Icons.Filled.MoreVert, stringResource(id = R.string.more))
                        DropdownMenu(
                            expanded = overflowMenuExpanded,
                            onDismissRequest = { overflowMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.delete)) },
                                onClick = {
                                    // TODO: Move to trash instead of permanent delete
                                    viewModel.deleteRecipe()
                                    onRecipeDelete()
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    BackButton(onClick = onNavigateUp)
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
                EditableIngredientList(
                    listState = listState,
                    header = {
                        AsyncImage(
                            model = editRecipeState.image ?: R.drawable.ic_launcher_background,
                            editRecipeState.title,
                            Modifier
                                .height(128.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    launcher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentScale = ContentScale.FillWidth,
                        )
                        Spacer(Modifier.height(8.dp))
                        TextEditField(
                            editRecipeState.title ?: "",
                            "Title",
                            onValueChange = { viewModel.updateName(it) }
                        )
                        TextEditField(
                            editRecipeState.description ?: "",
                            "Description",
                            onValueChange = { viewModel.updateDescription(it) }
                        )
                        Text(
                            "Ingredients",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(
                                horizontal = 18.dp,
                                vertical = 8.dp
                            )
                        )
                    },
                    ingredients = editRecipeState.ingredients,
                    onEditFieldValueChange = { index, new ->
                        viewModel.updateIngredient(index, new)
                    },
                    onDeleteIngredient = { viewModel.removeIngredient(it) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    weightUnit = weightUnit,
                    showWeight = showWeight,
                    onDeleteStep = { viewModel.removeStep(it) },
                    onStepFieldValueChange = { index, new -> viewModel.updateStep(index, new) },
                    steps = editRecipeState.steps,
                    onAddIngredient = { viewModel.newIngredient() },
                    onAddStep = { viewModel.newStep() }
                )
        }
    }

}

@Composable
fun EditableIngredientList(
    listState: LazyListState,
    ingredients: List<EditRecipeIngredientField>,
    steps: List<EditRecipeStepField>,
    modifier: Modifier = Modifier,
    onEditFieldValueChange: (Int, EditRecipeIngredientField) -> Unit,
    onStepFieldValueChange: (Int, EditRecipeStepField) -> Unit,
    onDeleteIngredient: (EditRecipeIngredientField) -> Unit,
    onAddIngredient: () -> Unit,
    onDeleteStep: (EditRecipeStepField) -> Unit,
    onAddStep: () -> Unit,
    header: @Composable LazyItemScope.() -> Unit,
    weightUnit: String,
    showWeight: Boolean
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
    ) {
        item(content = header)

        items(ingredients.size) { index ->
            val ingredientField = ingredients[index]
            IngredientEditField(
                name = ingredientField.name ?: "",
                percent = ingredientField.percent ?: "",
                onValueChange = { it ->
                    it.first?.let {
                        onEditFieldValueChange(
                            index,
                            ingredientField.copy(
                                name = it,
                            )
                        )
                    }
                    it.second?.let {
                        onEditFieldValueChange(
                            index,
                            ingredientField.copy(
                                percent = it,
                            )
                        )
                    }
                },
                onDeleteIngredient = { onDeleteIngredient(ingredientField) },
                weightUnit = weightUnit,
                showWeight = showWeight
            )
        }
        item {
            Button(
                onClick = onAddIngredient,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Add Ingredient")
            }
        }
        item {
            Text(
                "Steps",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 8.dp,
                )
            )
        }
        items(steps.size) {
                index ->
            val stepField = steps[index]
            StepEditField(
                description = stepField.description ?: "",
                duration = stepField.duration ?: "",
                onValueChange = { it ->
                    it.first?.let {
                        onStepFieldValueChange(
                            index,
                            stepField.copy(
                                description = it,
                            )
                        )
                    }
                    it.second?.let {
                        onStepFieldValueChange(
                            index,
                            stepField.copy(
                                duration = it,
                            )
                        )
                    }
                },
                onDeleteStep = { onDeleteStep(stepField) })
            Spacer(Modifier.height(8.dp))
        }
        item {
            Button(
                onClick = onAddStep,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Add Timed Step")
            }
        }
        item {
            Spacer(Modifier.height(78.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditField(
    name: String,
    percent: String,
    onValueChange: (Pair<String?, String?>) -> Unit,
    onDeleteIngredient: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    showWeight: Boolean,
    weightUnit: String
) {
    val trailingIconString = if(showWeight) weightUnit else "%"
    val inputLabel = if(showWeight) "Weight" else "Percent" // TODO: Extract strings

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        icon?.let {
            Icon(
                icon,
                name,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(32.dp)
                    .offset(y = 3.dp)
            ) }
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            name,
            onValueChange = { onValueChange(Pair(it, null)) },
            label = { Text("Ingredient") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions.Default
                .copy(imeAction = ImeAction.Done),
            isError = name == ""
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            percent,
            onValueChange = { onValueChange(Pair(null, it)) },
            label = { Text(inputLabel) },
            trailingIcon = { Text(trailingIconString) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions.Default
                .copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            isError = percent == ""
        )
        IconButton(onClick = onDeleteIngredient) {
            Icon(
                Icons.Outlined.Delete,
                "Delete",
                Modifier
                    .size(64.dp)
                    .padding(8.dp)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepEditField(
    description: String,
    duration: String,
    onValueChange: (Pair<String?, String?>) -> Unit,
    onDeleteStep: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        icon?.let {
            Icon(
                icon,
                description,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(32.dp)
                    .offset(y = 3.dp)
            ) }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(4f)) {
            OutlinedTextField(
                description,
                onValueChange = { onValueChange(Pair(it, null)) },
                label = { Text("Step description") },
                keyboardOptions = KeyboardOptions.Default
                    .copy(imeAction = ImeAction.Done),
                isError = description == "",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                duration,
                onValueChange = { onValueChange(Pair(null, it)) },
                label = { Text("Duration") },
                trailingIcon = { Text("min") },
                keyboardOptions = KeyboardOptions.Default
                    .copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                isError = duration == "",
                modifier = Modifier.fillMaxWidth()
            )
        }
        IconButton(
            modifier = Modifier.weight(0.5f),
            onClick = onDeleteStep
        ) {
            Icon(
                Icons.Outlined.Delete,
                "Delete",
                Modifier
                    .size(64.dp)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStepEditField()
{
    BakersRecipesTheme() {
        StepEditField(
            description = "Bollire la pasta",
            duration = "12",
            onValueChange = {},
            onDeleteStep = { /*TODO*/ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditField(
    name: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
        OutlinedTextField(
            name,
            onValueChange = onValueChange,
            label = { Text(label) },
            keyboardOptions = KeyboardOptions.Default
                .copy(imeAction = ImeAction.Done),
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp,
                    horizontal = 12.dp
                )
        )
}
