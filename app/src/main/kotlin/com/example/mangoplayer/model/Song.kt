package com.example.mangoplayer.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,          // ms
    val uri: Uri,
    val albumArtUri: Uri? = null,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val dateAdded: Long = 0L,
    val size: Long = 0L,
    val path: String = ""
)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: Uri? = null,
    val year: Int = 0,
    val songs: List<Song> = emptyList()
)

data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int,
    val songCount: Int,
    val albums: List<Album> = emptyList()
)

data class Playlist(
    val id: Long,
    val name: String,
    val songs: List<Song> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

sealed class RepeatMode {
    object Off    : RepeatMode()
    object One    : RepeatMode()
    object All    : RepeatMode()
}
