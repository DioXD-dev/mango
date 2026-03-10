package com.example.mangoplayer.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mangoplayer.model.RepeatMode
import com.example.mangoplayer.ui.screen.formatDuration
import com.example.mangoplayer.viewmodel.PlayerViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    onLyrics: () -> Unit
) {
    val song by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val isShuffling by viewModel.isShuffling.collectAsStateWithLifecycle()
    val albumBitmap by viewModel.albumBitmap.collectAsStateWithLifecycle()
    var isSeeking by remember { mutableStateOf(false) }
    var seekValue by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Blurred background ─────────────────────────────────────────────────
        if (albumBitmap != null) {
            AsyncImage(
                model = song?.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp)
            )
        } else {
            Box(
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // Scrim overlay for readability
        Box(
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.4f),
                        0.4f to Color.Black.copy(alpha = 0.6f),
                        1f to Color.Black.copy(alpha = 0.85f)
                    )
                )
        )

        // ── Content ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.KeyboardArrowDown, "Back", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("Now Playing", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { /* queue sheet */ }) {
                    Icon(Icons.Default.QueueMusic, "Queue", tint = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Album art with swipe gesture
            var dragX by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragX > 80) viewModel.previous()
                                else if (dragX < -80) viewModel.next()
                                dragX = 0f
                            },
                            onHorizontalDrag = { _, dx -> dragX += dx }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song?.albumArtUri,
                    contentDescription = "Album art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .animateContentSize()
                )
            }

            Spacer(Modifier.height(32.dp))

            // Song info
            Text(
                text = song?.title ?: "No song selected",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${song?.artist ?: ""} • ${song?.album ?: ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            // Seek bar
            val sliderPos = if (isSeeking) seekValue else {
                if (duration > 0) progress.toFloat() / duration.toFloat() else 0f
            }
            Slider(
                value = sliderPos,
                onValueChange = { isSeeking = true; seekValue = it },
                onValueChangeFinished = {
                    viewModel.seekTo((seekValue * duration).toLong())
                    isSeeking = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(progress), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Text(formatDuration(duration), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }

            Spacer(Modifier.height(24.dp))

            // Controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle, "Shuffle",
                        tint = if (isShuffling) MaterialTheme.colorScheme.primary else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Previous
                IconButton(onClick = { viewModel.previous() }) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                // Play/Pause
                FilledIconButton(
                    onClick = { viewModel.playPause() },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Play/Pause",
                        modifier = Modifier.size(40.dp)
                    )
                }
                // Next
                IconButton(onClick = { viewModel.next() }) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                // Repeat
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    Icon(
                        when (repeatMode) {
                            RepeatMode.One -> Icons.Default.RepeatOne
                            else           -> Icons.Default.Repeat
                        },
                        "Repeat",
                        tint = if (repeatMode != RepeatMode.Off) MaterialTheme.colorScheme.primary else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Extra actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onLyrics) {
                    Icon(Icons.Default.Lyrics, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Lyrics", color = Color.White)
                }
                TextButton(onClick = { /* equalizer */ }) {
                    Icon(Icons.Default.Equalizer, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("EQ", color = Color.White)
                }
                TextButton(onClick = { /* add to playlist */ }) {
                    Icon(Icons.Default.PlaylistAdd, null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Playlist", color = Color.White)
                }
            }
        }
    }
}
