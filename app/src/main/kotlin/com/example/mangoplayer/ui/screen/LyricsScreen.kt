package com.example.mangoplayer.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mangoplayer.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ── LRC Line model ────────────────────────────────────────────────────────────

data class LrcLine(val timeMs: Long, val text: String)

// ── LRCLIB API ────────────────────────────────────────────────────────────────

data class LrclibResponse(
    val syncedLyrics: String?,
    val plainLyrics: String?
)

interface LrclibApi {
    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") track: String,
        @Query("artist_name") artist: String,
        @Query("album_name") album: String
    ): retrofit2.Response<LrclibResponse>
}

object LyricsRepository {
    private val api = Retrofit.Builder()
        .baseUrl("https://lrclib.net/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LrclibApi::class.java)

    suspend fun fetchSynced(track: String, artist: String, album: String): List<LrcLine>? =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.getLyrics(track, artist, album)
                val body = resp.body() ?: return@withContext null
                val lrc  = body.syncedLyrics ?: return@withContext null
                parseLrc(lrc)
            } catch (e: Exception) { null }
        }

    fun parseLrc(raw: String): List<LrcLine> {
        val regex = Regex("""^\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)$""")
        return raw.lines().mapNotNull { line ->
            regex.matchEntire(line.trim())?.destructured?.let { (min, sec, ms, text) ->
                val timeMs = min.toLong() * 60_000 + sec.toLong() * 1_000 +
                             (if (ms.length == 2) ms.toLong() * 10 else ms.toLong())
                LrcLine(timeMs, text.trim())
            }
        }.sortedBy { it.timeMs }
    }
}

// ── Lyrics Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val song by viewModel.currentSong.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    var lines by remember { mutableStateOf<List<LrcLine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Fetch lyrics when song changes
    LaunchedEffect(song) {
        val s = song ?: return@LaunchedEffect
        isLoading = true
        error = null
        val result = LyricsRepository.fetchSynced(s.title, s.artist, s.album)
        if (result != null && result.isNotEmpty()) {
            lines = result
        } else {
            error = "Lyrics not found for \"${s.title}\""
        }
        isLoading = false
    }

    // Auto-scroll to active line
    val activeIndex = remember(progress, lines) {
        if (lines.isEmpty()) return@remember -1
        var idx = lines.indexOfLast { it.timeMs <= progress }
        if (idx < 0) idx = 0
        idx
    }
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && lines.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(
                    index = (activeIndex - 2).coerceAtLeast(0)
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(song?.title ?: "Lyrics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(lines) { idx, line ->
                        val isActive = idx == activeIndex
                        val textColor by animateColorAsState(
                            targetValue = if (isActive) MaterialTheme.colorScheme.primary
                                          else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            animationSpec = tween(300),
                            label = "lyric_color"
                        )
                        Text(
                            text = line.text,
                            fontSize = if (isActive) 22.sp else 17.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
