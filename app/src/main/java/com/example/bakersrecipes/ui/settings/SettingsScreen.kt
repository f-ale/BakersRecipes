package com.example.bakersrecipes.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreBooleanSettingState
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.example.bakersrecipes.R
import com.example.bakersrecipes.ui.common.BackButton
import com.example.bakersrecipes.ui.theme.BakersRecipesTheme

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview()
{
    BakersRecipesTheme {
        // SettingsScreen {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    dataStore: DataStore<Preferences>
)
{
    val weightUnitPreferenceState =
        rememberPreferenceDataStoreBooleanSettingState(
            key = "weight_unit",
            defaultValue = false,
            dataStore = dataStore
        )

    val showWeightsInsteadOfPercentPreferenceState =
        rememberPreferenceDataStoreBooleanSettingState(
            key = "show_weight",
            defaultValue = false,
            dataStore = dataStore
        )

    Scaffold(topBar = {
        TopAppBar(
            title = { Text( stringResource(R.string.settings))},
             navigationIcon = {
                BackButton(onClick = onBackPressed)
            }
        )
    }) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            SettingsMenuLink(
                title = { Text(stringResource(R.string.weight_unit)) },
                subtitle = { Text(stringResource(R.string.setting_weight_unit_description)) },
                action = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.gram_unit),
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.Center
                            )
                            RadioButton(
                                selected = !weightUnitPreferenceState.value,
                                onClick = { weightUnitPreferenceState.value = false }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.oz_unit),
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.Center
                            )
                            RadioButton(
                                selected = weightUnitPreferenceState.value,
                                onClick = { weightUnitPreferenceState.value = true }
                            )
                        }
                    }
            }) {}

            SettingsSwitch(
                state = showWeightsInsteadOfPercentPreferenceState,
                title = { Text(stringResource(R.string.show_weights_by_default))},
                subtitle = {Text(stringResource(R.string.show_weights_instead_of_percentages))}
            )
        }
    }



}