package com.example.bakersrecipes.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.bakersrecipes.R
import com.example.bakersrecipes.data.AlarmStates
import com.example.bakersrecipes.data.StepState
import com.example.bakersrecipes.ui.common.BackButton
import com.example.bakersrecipes.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val context = LocalContext.current

    // A surface container using the 'background' color from the theme
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
                 TopAppBar(
                     title = {
                         Text(
                             recipeDetailState.recipe?.name ?: "",
                             maxLines = 1,
                             overflow = TextOverflow.Ellipsis
                         )
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
                     },
                     scrollBehavior = scrollBehavior
                 )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn {
                recipeDetailState.recipe?.description?.let { description ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Column {
                                AsyncImage(
                                    model =
                                    recipeDetailState.recipe?.image
                                        ?: R.drawable.ic_launcher_background,
                                    recipeDetailState.recipe?.name
                                        ?: "Thumbnail",
                                    Modifier
                                        .height(128.dp)
                                        .fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                ingredientList(
                    ingredients = recipeDetailState.ingredientDisplayList,
                    showWeight = recipeDetailState.totalRecipeWeight != null,
                    weightUnit = weightUnit,
                    paddingHorizontal = 24.dp,
                    paddingBottom = 16.dp,
                )

                if(recipeDetailState.ingredientDisplayList.isNotEmpty())
                {
                    item {
                        //Divider()
                    }
                    item {
                        MakeRecipeForm(
                            recipeDetailState.totalRecipeWeight?.toString() ?: "",
                            onUpdateTotalRecipeWeight =
                            { viewModel.updateMakeRecipeWeightFromString(it) },
                            weightUnit = weightUnit,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }
                }
                if(recipeDetailState.stepDisplayList.isNotEmpty())
                {
                    item {
                        //Divider(modifier = Modifier.padding(bottom = 16.dp))
                    }

                    stepsList(
                        steps = recipeDetailState.stepDisplayList,
                        onAlarmSet = { id ->
                                viewModel.setAlarm(id)
                            },
                        onAlarmCanceled = {
                                id -> viewModel.cancelAlarm(id)
                            },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
fun Long.toTimeDurationString(): String { // TODO: Move somewhere better
    val hours = this / 1000 / 3600
    val minutes = (this / 1000 / 60) % 60
    val seconds = (this / 1000) % 60

    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

fun LazyListScope.stepsList(
    steps: List<StateFlow<StepState>>,
    onAlarmSet: (Int) -> Unit,
    onAlarmCanceled: (Int) -> Unit,
    modifier: Modifier = Modifier
)
{
    item {
        Text(
            "Timers",
            style = Typography.titleMedium,
            modifier = modifier
        )
    }
    item {
        Spacer(Modifier.height(8.dp))
    }
    items(steps) { step ->
        StepItem(step, onAlarmSet, onAlarmCanceled, modifier = modifier)
    }
}

@Composable
fun StepItem(
    step: StateFlow<StepState>,
    onAlarmSet: (Int) -> Unit,
    onAlarmCanceled: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val stepState = step.collectAsStateWithLifecycle()

    // TODO: Refactor in a separate composable
    var remainingTime by remember(stepState.value.alarmState.scheduledTime) {
        mutableLongStateOf(
            if(stepState.value.alarmState.scheduledTime - System.currentTimeMillis() > 0)
            {
                stepState.value.alarmState.scheduledTime - System.currentTimeMillis()
            } else {
                0L
            }
        )
    }

    LaunchedEffect(remainingTime) {
        val diff = remainingTime - (stepState.value.alarmState.scheduledTime - System.currentTimeMillis())
        delay(1_000L - diff)
        remainingTime = if(remainingTime > 0L) {
            stepState.value.alarmState.scheduledTime - System.currentTimeMillis()
        } else {
            0L
        }
    }

    val cardColors =
        if(stepState.value.alarmState.state == AlarmStates.RINGING ||
            (stepState.value.alarmState.state == AlarmStates.SCHEDULED
            && remainingTime == 0L)
        )
            CardDefaults.cardColors(containerColor = Color.Red) // TODO: Change color
        else {
            CardDefaults.cardColors()
        }

    Card(
        colors = cardColors,
        modifier = modifier
    ) {
        Box {
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
                        stepState.value.stepId.toString() + ".",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = modifier.padding(horizontal = 2.dp)
                    )
                    Text(
                        stepState.value.description,
                        Modifier
                            .padding(4.dp)
                            .weight(4f)
                    )
                    Text(
                        if(stepState.value.alarmState.state == AlarmStates.SCHEDULED) {
                            remainingTime
                                .toTimeDurationString()
                        }
                        else
                        {
                            stepState.value.duration.roundToInt().toString()+" min"
                        },
                        textAlign = TextAlign.Center
                    )
                }
                if(stepState.value.alarmState.state == AlarmStates.SCHEDULED
                    || stepState.value.alarmState.state == AlarmStates.RINGING)
                {
                    IconButton(
                        onClick = { onAlarmCanceled(stepState.value.stepId) }
                    ) {
                        Icon(Icons.Default.Close, "Cancel Alarm")
                    }
                } else if(stepState.value.alarmState.state == AlarmStates.INACTIVE) {
                    IconButton(
                        onClick = { onAlarmSet(stepState.value.stepId) }
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
    weightUnit: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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