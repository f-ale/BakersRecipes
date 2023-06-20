package com.example.bakersrecipes.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreBooleanSettingState
import com.alorma.compose.settings.ui.SettingsSwitch
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
            title = { Text("Settings")},
             navigationIcon = {
                BackButton(onClick = onBackPressed)
            }
        )
    }) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            SettingsSwitch(
                state = weightUnitPreferenceState,
                title = { Text("Weight Unit") },
                subtitle = { Text("Select the default unit used for weight measurements.") }
            )
            SettingsSwitch(
                state = showWeightsInsteadOfPercentPreferenceState,
                title = { Text("Show weights by default")},
                subtitle = {Text("Show weights instead of percentages")}
            )
        }
    }



}