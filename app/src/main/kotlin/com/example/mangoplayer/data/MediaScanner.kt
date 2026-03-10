package com.example.mangoplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.example.mangoplayer.model.Album
import com.example.mangoplayer.model.Artist
import com.example.mangoplayer.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaScanner {

    private val AUDIO_URI: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")

    private val SONG_PROJECTION = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATA
    )

    suspend fun scanSongs(resolver: ContentResolver): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        resolver.query(
            AUDIO_URI,
            SONG_PROJECTION,
            "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND ${MediaStore.Audio.Media.DURATION} >= 10000",
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idCol      = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val pathCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id      = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                songs.add(
                    Song(
                        id          = id,
                        title       = cursor.getString(titleCol) ?: "Unknown",
                        artist      = cursor.getString(artistCol) ?: "Unknown Artist",
                        album       = cursor.getString(albumCol) ?: "Unknown Album",
                        duration    = cursor.getLong(durCol),
                        uri         = ContentUris.withAppendedId(AUDIO_URI, id),
                        albumArtUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId),
                        trackNumber = cursor.getInt(trackCol),
                        year        = cursor.getInt(yearCol),
                        dateAdded   = cursor.getLong(dateCol),
                        size        = cursor.getLong(sizeCol),
                        path        = cursor.getString(pathCol) ?: ""
                    )
                )
            }
        }
        songs
    }

    fun groupByAlbum(songs: List<Song>): List<Album> =
        songs.groupBy { it.album }.map { (albumName, albumSongs) ->
            Album(
                id          = albumSongs.first().id,
                name        = albumName,
                artist      = albumSongs.first().artist,
                songCount   = albumSongs.size,
                albumArtUri = albumSongs.first().albumArtUri,
                year        = albumSongs.maxOf { it.year },
                songs       = albumSongs.sortedBy { it.trackNumber }
            )
        }.sortedBy { it.name }

    fun groupByArtist(songs: List<Song>): List<Artist> =
        songs.groupBy { it.artist }.map { (artistName, artistSongs) ->
            val albums = groupByAlbum(artistSongs)
            Artist(
                id         = artistSongs.first().id,
                name       = artistName,
                albumCount = albums.size,
                songCount  = artistSongs.size,
                albums     = albums
            )
        }.sortedBy { it.name }
}
