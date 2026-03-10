package com.example.mangoplayer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mangoplayer.ui.screen.*
import com.example.mangoplayer.viewmodel.PlayerViewModel

sealed class Screen(val route: String) {
    object Library    : Screen("library")
    object NowPlaying : Screen("now_playing")
    object Lyrics     : Screen("lyrics")
    object Albums     : Screen("albums")
    object Artists    : Screen("artists")
    object Playlists  : Screen("playlists")
    object Settings   : Screen("settings")
}

@Composable
fun MangoNavHost(
    navController: NavHostController,
    playerViewModel: PlayerViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Library.route) {
        composable(Screen.Library.route) {
            LibraryScreen(
                viewModel = playerViewModel,
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                    navController.navigate(Screen.NowPlaying.route)
                },
                onNavigate = { navController.navigate(it) }
            )
        }
        composable(Screen.NowPlaying.route) {
            NowPlayingScreen(
                viewModel = playerViewModel,
                onBack = { navController.popBackStack() },
                onLyrics = { navController.navigate(Screen.Lyrics.route) }
            )
        }
        composable(Screen.Lyrics.route) {
            LyricsScreen(
                viewModel = playerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Albums.route) {
            AlbumsScreen(viewModel = playerViewModel, onNavigate = { navController.navigate(it) })
        }
        composable(Screen.Artists.route) {
            ArtistsScreen(viewModel = playerViewModel, onNavigate = { navController.navigate(it) })
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen(viewModel = playerViewModel, onNavigate = { navController.navigate(it) })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
