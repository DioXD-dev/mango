package com.example.mangoplayer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mangoplayer.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    viewModel: PlayerViewModel,
    onNavigate: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Placeholder playlists list (integrate with Room in production)
    val playlists = remember { mutableStateListOf("Favorites", "Workout", "Chill Vibes") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Playlists") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Create playlist")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            // Default queues
            item {
                ListItem(
                    modifier = Modifier.clickable { },
                    leadingContent = { Icon(Icons.Default.FavoriteBorder, null) },
                    headlineContent = { Text("Liked Songs") }
                )
                HorizontalDivider()
            }
            item {
                ListItem(
                    modifier = Modifier.clickable { },
                    leadingContent = { Icon(Icons.Default.History, null) },
                    headlineContent = { Text("Recently Played") }
                )
                HorizontalDivider()
            }

            // User playlists
            items(playlists) { name ->
                ListItem(
                    modifier = Modifier.clickable { },
                    leadingContent = { Icon(Icons.Default.QueueMusic, null) },
                    headlineContent = { Text(name) },
                    trailingContent = {
                        IconButton(onClick = { playlists.remove(name) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; newPlaylistName = "" },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        playlists.add(newPlaylistName.trim())
                    }
                    showCreateDialog = false
                    newPlaylistName = ""
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newPlaylistName = "" }) { Text("Cancel") }
            }
        )
    }
}
