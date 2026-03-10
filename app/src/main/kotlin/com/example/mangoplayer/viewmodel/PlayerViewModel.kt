package com.example.mangoplayer.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.imageLoader
import coil.request.ImageRequest
import com.example.mangoplayer.data.MediaScanner
import com.example.mangoplayer.model.RepeatMode
import com.example.mangoplayer.model.Song
import com.example.mangoplayer.service.AudioService
import com.example.mangoplayer.ui.theme.generatePalette
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>()

    // ── Library state ──────────────────────────────────────────────────────────
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── Playback state ─────────────────────────────────────────────────────────
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(1L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.Off)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isShuffling = MutableStateFlow(false)
    val isShuffling: StateFlow<Boolean> = _isShuffling.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    // ── UI state ───────────────────────────────────────────────────────────────
    private val _albumBitmap = MutableStateFlow<Bitmap?>(null)
    val albumBitmap: StateFlow<Bitmap?> = _albumBitmap.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> = combine(_songs, _searchQuery) { list, q ->
        if (q.isBlank()) list
        else list.filter {
            it.title.contains(q, ignoreCase = true) ||
            it.artist.contains(q, ignoreCase = true) ||
            it.album.contains(q, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Media3 controller ─────────────────────────────────────────────────────
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) { _isPlaying.value = playing }
        override fun onMediaItemTransition(item: MediaItem?, reason: Int) { syncCurrentSong() }
        override fun onPlaybackStateChanged(state: Int) { if (state == Player.STATE_READY) _duration.value = controller?.duration ?: 1L }
        override fun onRepeatModeChanged(mode: Int) {
            _repeatMode.value = when (mode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.One
                Player.REPEAT_MODE_ALL -> RepeatMode.All
                else                   -> RepeatMode.Off
            }
        }
        override fun onShuffleModeEnabledChanged(enabled: Boolean) { _isShuffling.value = enabled }
    }

    init {
        scanLibrary()
        connectController()
        tickProgress()
    }

    // ── Library ────────────────────────────────────────────────────────────────

    private fun scanLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            val scanned = MediaScanner.scanSongs(context.contentResolver)
            _songs.value = scanned
            _isLoading.value = false
        }
    }

    fun refreshLibrary() = scanLibrary()

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    // ── Controller ─────────────────────────────────────────────────────────────

    private fun connectController() {
        val token = SessionToken(context, android.content.ComponentName(context, AudioService::class.java))
        controllerFuture = MediaController.Builder(context, token).buildAsync()
        controllerFuture!!.addListener({
            controller = controllerFuture!!.get()
            controller?.addListener(playerListener)
            syncCurrentSong()
        }, MoreExecutors.directExecutor())
    }

    private fun syncCurrentSong() {
        val idx = controller?.currentMediaItemIndex ?: return
        val q = _queue.value
        if (idx in q.indices) {
            _currentSong.value = q[idx]
            loadAlbumBitmap(q[idx])
        }
    }

    private fun loadAlbumBitmap(song: Song) {
        viewModelScope.launch {
            val req = ImageRequest.Builder(context)
                .data(song.albumArtUri)
                .allowHardware(false)
                .size(512, 512)
                .build()
            val result = context.imageLoader.execute(req)
            val bmp = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            _albumBitmap.value = bmp
            if (bmp != null) {
                // Palette generated here but consumed via MangoTheme state
            }
        }
    }

    private fun tickProgress() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                _progress.value = controller?.currentPosition ?: 0L
            }
        }
    }

    // ── Playback controls ──────────────────────────────────────────────────────

    fun playSong(song: Song, queue: List<Song> = _songs.value) {
        _queue.value = queue
        val ctrl = controller ?: return
        val items = queue.map { MediaItem.fromUri(it.uri) }
        ctrl.setMediaItems(items, queue.indexOf(song), 0)
        ctrl.prepare()
        ctrl.play()
    }

    fun playPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun seekTo(posMs: Long) { controller?.seekTo(posMs) }

    fun next() { controller?.seekToNextMediaItem() }

    fun previous() {
        val ctrl = controller ?: return
        if (ctrl.currentPosition > 3000) ctrl.seekTo(0)
        else ctrl.seekToPreviousMediaItem()
    }

    fun toggleShuffle() { controller?.shuffleModeEnabled = !(_isShuffling.value) }

    fun toggleRepeat() {
        val ctrl = controller ?: return
        ctrl.repeatMode = when (_repeatMode.value) {
            RepeatMode.Off -> Player.REPEAT_MODE_ALL
            RepeatMode.All -> Player.REPEAT_MODE_ONE
            RepeatMode.One -> Player.REPEAT_MODE_OFF
        }
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
