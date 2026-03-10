package com.example.mangoplayer.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette

// Shared palette state accessible throughout the app
val LocalPalette = compositionLocalOf<Palette?> { null }

@Composable
fun MangoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var palette by remember { mutableStateOf<Palette?>(null) }

    val colorScheme = remember(palette, darkTheme) {
        when {
            // Material You dynamic colors on Android 12+
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            }
            // Fallback: palette-based or default
            palette != null -> buildPaletteColorScheme(palette!!, darkTheme)
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
    }

    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MangoTypography,
            content = content
        )
    }
}

/** Build a ColorScheme derived from album art palette */
fun buildPaletteColorScheme(palette: Palette, dark: Boolean): ColorScheme {
    val dominant = palette.getDominantColor(0xFF1A1A2E.toInt())
    val vibrant  = palette.getVibrantColor(dominant)
    val muted    = palette.getMutedColor(dominant)

    val primary   = Color(vibrant)
    val secondary = Color(muted)
    val bg        = if (dark) Color(0xFF0F0F1A) else Color(0xFFF5F5FF)

    return if (dark) darkColorScheme(
        primary   = primary,
        secondary = secondary,
        background = bg,
        surface   = bg,
        onPrimary = Color.White
    ) else lightColorScheme(
        primary   = primary,
        secondary = secondary,
        background = bg,
        surface   = bg
    )
}

/** Generate a Palette from a Bitmap (call in a coroutine / LaunchedEffect) */
suspend fun generatePalette(bitmap: Bitmap): Palette =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        Palette.from(bitmap).generate { p ->
            if (p != null) cont.resume(p) {} else cont.cancel()
        }
    }
