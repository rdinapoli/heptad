package com.heptad.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.heptad.app.data.preferences.ThemeMode
import com.heptad.app.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Dictionary Level Section
            SettingsSection(title = "Dictionary") {
                DictionaryLevelOption(
                    selectedLevel = preferences.dictionaryLevel,
                    onLevelSelected = { viewModel.setDictionaryLevel(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Include S Toggle
                SettingsSwitchItem(
                    title = "Include letter 'S'",
                    subtitle = "Allow puzzles to include the letter S",
                    checked = preferences.includeS,
                    onCheckedChange = { viewModel.setIncludeS(it) }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Theme Section
            SettingsSection(title = "Appearance") {
                ThemeModeOption(
                    selectedMode = preferences.themeMode,
                    onModeSelected = { viewModel.setThemeMode(it) }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSection(title = "About") {
                Text(
                    text = "Heptad v1.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A word puzzle game inspired by the New York Times Spelling Bee",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun DictionaryLevelOption(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        Text(
            text = "Word list size",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DictionaryLevelItem(
            level = 60,
            title = "Common (~75k words)",
            subtitle = "Easier - only common words",
            selected = selectedLevel == 60,
            onSelect = { onLevelSelected(60) }
        )

        DictionaryLevelItem(
            level = 70,
            title = "Standard (~110k words)",
            subtitle = "Recommended - balanced difficulty",
            selected = selectedLevel == 70,
            onSelect = { onLevelSelected(70) }
        )

        DictionaryLevelItem(
            level = 80,
            title = "Comprehensive (~235k words)",
            subtitle = "Harder - includes rare words",
            selected = selectedLevel == 80,
            onSelect = { onLevelSelected(80) }
        )
    }
}

@Composable
private fun DictionaryLevelItem(
    level: Int,
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThemeModeItem(
            mode = ThemeMode.SYSTEM,
            title = "System default",
            selected = selectedMode == ThemeMode.SYSTEM,
            onSelect = { onModeSelected(ThemeMode.SYSTEM) }
        )

        ThemeModeItem(
            mode = ThemeMode.LIGHT,
            title = "Light",
            selected = selectedMode == ThemeMode.LIGHT,
            onSelect = { onModeSelected(ThemeMode.LIGHT) }
        )

        ThemeModeItem(
            mode = ThemeMode.DARK,
            title = "Dark",
            selected = selectedMode == ThemeMode.DARK,
            onSelect = { onModeSelected(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun ThemeModeItem(
    mode: ThemeMode,
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
