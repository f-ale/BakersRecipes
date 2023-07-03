package com.example.bakersrecipes.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.bakersrecipes.R
import com.example.bakersrecipes.data.AlarmStates
import com.example.bakersrecipes.data.StepState
import com.example.bakersrecipes.ui.common.BackButton
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme
import kotlin.math.roundToInt

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview()
{
    /*
    BakersRecipesTheme {
        RecipeDetailScreen(DetailViewModel())
    }*/
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen( // TODO: Make scrollable
    viewModel: DetailViewModel,
    onNavigateUp: () -> Unit,
    onEditRecipe: (Int) -> Unit
) {
    val recipeDetailState by viewModel.recipeDetailState.collectAsStateWithLifecycle()
    val weightUnit by viewModel.getWeightUnit().collectAsStateWithLifecycle("g")

    val context = LocalContext.current

    // A surface container using the 'background' color from the theme
    Scaffold(
        topBar = {
                 MediumTopAppBar(
                     title = {
                         Text(recipeDetailState.recipe?.name ?: "")
                     },
                     navigationIcon = {
                        BackButton(onClick = onNavigateUp)
                 }, actions = {
                         IconButton(
                             onClick = { onEditRecipe(viewModel.recipeId) },
                             content = { Icon(Icons.Outlined.Edit, "Edit") }
                         )
                         IconButton(
                             onClick = {
                                startActivity(
                                    context,
                                    viewModel.getShareIntent(),
                                    null
                                )
                             },
                             content = { Icon(Icons.Outlined.Share, "Share") }
                         )
                     }

                 )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column {
                AsyncImage(
                    model = recipeDetailState.recipe?.image ?: R.drawable.ic_launcher_background,
                    recipeDetailState.recipe?.name ?: "Thumbnail",
                    Modifier
                        .height(248.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
                ) {
                    recipeDetailState.recipe?.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IngredientList(
                        ingredients = recipeDetailState.ingredientDisplayList,
                        showWeight = recipeDetailState.totalRecipeWeight != null,
                        weightUnit = weightUnit
                    )
                    if(recipeDetailState.ingredientDisplayList.isNotEmpty())
                    {
                        Divider()
                        MakeRecipeForm(
                            recipeDetailState.totalRecipeWeight?.toString() ?: "",
                            onUpdateTotalRecipeWeight =
                            { viewModel.updateMakeRecipeWeightFromString(it) },
                            weightUnit = weightUnit
                        )
                    }
                    if(recipeDetailState.stepDisplayList.isNotEmpty())
                    {
                        Divider()
                        StepsList(
                            steps = recipeDetailState.stepDisplayList,
                            onAlarmSet = { id, duration ->
                                viewModel.setAlarm(id, duration)
                            },
                            onAlarmCanceled = {
                                id -> viewModel.cancelAlarm(id)
                            },
                        )
                    }
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun StepsListPreview()
{
    BakersRecipesTheme {
        StepsList(
            listOf(
                StepState(1,"Boil the water", 10f)
            ),
            { _, _ -> },
            {}
        )
    }
}
@Composable
fun StepsList(
    steps: List<StepState>,
    onAlarmSet: (Int, Int) -> Unit,
    onAlarmCanceled: (Int) -> Unit,
    modifier: Modifier = Modifier
)
{
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(steps) { step ->
            StepItem(step, onAlarmSet, onAlarmCanceled)
        }
    }
}

@Composable
fun StepItem(
    step: StepState,
    onAlarmSet: (Int, Int) -> Unit,
    onAlarmCanceled: (Int) -> Unit,
    modifier: Modifier = Modifier
)
{
    val cardColors =
        if(step.alarmState.state == AlarmStates.RINGING)
            CardDefaults.cardColors(containerColor = Color.Red) // TODO: Change color
        else {
            CardDefaults.cardColors()
        }

    Card(
        colors = cardColors
    ) {
        Box (modifier = modifier) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
                ) {
                    Text(
                        step.stepId.toString() + ".",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        step.description,
                        Modifier
                            .padding(8.dp)
                            .weight(4f)
                    )

                    Text(
                        if(step.alarmState.state == AlarmStates.SCHEDULED) {
                            (step.alarmState.remainingTime.toFloat() / (60*1000)).toString()
                        }
                        else
                        {
                            step.duration.roundToInt().toString()+" min"
                        },
                        textAlign = TextAlign.Center
                    )
                }
                if(step.alarmState.state == AlarmStates.SCHEDULED
                    || step.alarmState.state == AlarmStates.RINGING)
                {
                    IconButton(
                        onClick = { onAlarmCanceled(step.stepId) }
                    ) {
                        Icon(Icons.Default.Close, "Cancel Alarm")
                    }
                } else if(step.alarmState.state == AlarmStates.INACTIVE) {
                    IconButton(
                        onClick = { onAlarmSet(step.stepId, step.duration.roundToInt()) }
                    ) {
                        Icon(Icons.Default.PlayArrow, "Set Alarm")
                    }
                }

            }

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeRecipeForm(
    totalRecipeWeight: String,
    onUpdateTotalRecipeWeight: (String) -> Unit,
    weightUnit: String
) {
    Column {
        Text(
            stringResource(id = R.string.make_recipe),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                totalRecipeWeight,
                onValueChange = onUpdateTotalRecipeWeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text(stringResource(id = R.string.total_recipe_weight)) },
                trailingIcon = { Text(weightUnit) },
                keyboardOptions = KeyboardOptions
                    .Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
}