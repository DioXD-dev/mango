package com.example.mangoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.mangoplayer.ui.navigation.MangoNavHost
import com.example.mangoplayer.ui.theme.MangoTheme
import com.example.mangoplayer.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* handled in viewmodel */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request media permissions
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO))
        } else {
            permissionLauncher.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        setContent {
            val playerViewModel: PlayerViewModel = viewModel()
            MangoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    MangoNavHost(
                        navController = navController,
                        playerViewModel = playerViewModel
                    )
                }
            }
        }
    }
}
