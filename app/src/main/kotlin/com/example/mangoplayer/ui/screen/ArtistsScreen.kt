package com.example.mangoplayer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mangoplayer.data.MediaScanner
import com.example.mangoplayer.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(
    viewModel: PlayerViewModel,
    onNavigate: (String) -> Unit
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val artists = remember(songs) { MediaScanner.groupByArtist(songs) }

    Scaffold(topBar = { TopAppBar(title = { Text("Artists") }) }) { padding ->
        LazyColumn(contentPadding = padding) {
            items(artists, key = { it.id }) { artist ->
                ListItem(
                    modifier = Modifier.clickable {
                        artist.albums.firstOrNull()?.songs?.firstOrNull()?.let { song ->
                            val allSongs = artist.albums.flatMap { it.songs }
                            viewModel.playSong(song, allSongs)
                            onNavigate("now_playing")
                        }
                    },
                    leadingContent = {
                        Box(Modifier.size(48.dp).clip(CircleShape)) {
                            val art = artist.albums.firstOrNull()?.albumArtUri
                            if (art != null) {
                                AsyncImage(model = art, contentDescription = null,
                                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(Icons.Default.Person, null,
                                    modifier = Modifier.fillMaxSize().padding(8.dp))
                            }
                        }
                    },
                    headlineContent = { Text(artist.name) },
                    supportingContent = { Text("${artist.albumCount} albums • ${artist.songCount} songs") }
                )
                HorizontalDivider()
            }
        }
    }
}
