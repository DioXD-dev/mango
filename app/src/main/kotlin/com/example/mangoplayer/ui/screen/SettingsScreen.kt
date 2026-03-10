package com.example.mangoplayer.ui.screen

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var dynamicColor by remember { mutableStateOf(true) }
    var darkTheme by remember { mutableStateOf(true) }
    var gaplessPlayback by remember { mutableStateOf(false) }
    var crossfadeDuration by remember { mutableFloatStateOf(0f) }
    var showLyricsOnLockscreen by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {

            // Appearance
            item {
                ListItem(
                    headlineContent = { Text("Appearance", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary) }
                )
            }
            if (Build.VERSION.SDK_INT >= 31) {
                item {
                    ListItem(
                        headlineContent = { Text("Material You (Dynamic Color)") },
                        supportingContent = { Text("Use wallpaper colors for the theme") },
                        trailingContent = {
                            Switch(checked = dynamicColor, onCheckedChange = { dynamicColor = it })
                        }
                    )
                    HorizontalDivider()
                }
            }
            item {
                ListItem(
                    headlineContent = { Text("Dark Theme") },
                    trailingContent = { Switch(checked = darkTheme, onCheckedChange = { darkTheme = it }) }
                )
                HorizontalDivider()
            }

            // Playback
            item {
                ListItem(
                    headlineContent = { Text("Playback", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Gapless Playback") },
                    supportingContent = { Text("Remove silence between tracks") },
                    trailingContent = {
                        Switch(checked = gaplessPlayback, onCheckedChange = { gaplessPlayback = it })
                    }
                )
                HorizontalDivider()
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Crossfade: ${crossfadeDuration.toInt()}s",
                        style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = crossfadeDuration,
                        onValueChange = { crossfadeDuration = it },
                        valueRange = 0f..12f,
                        steps = 11
                    )
                }
                HorizontalDivider()
            }

            // Lyrics
            item {
                ListItem(
                    headlineContent = { Text("Lyrics", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Show on lock screen") },
                    trailingContent = {
                        Switch(checked = showLyricsOnLockscreen, onCheckedChange = { showLyricsOnLockscreen = it })
                    }
                )
                HorizontalDivider()
            }

            // About
            item {
                ListItem(
                    headlineContent = { Text("About", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("MangoPlayer") },
                    supportingContent = { Text("v1.0.0 • Material You Music Player") },
                    leadingContent = { Text("🥭", style = MaterialTheme.typography.titleLarge) }
                )
            }
        }
    }
}
