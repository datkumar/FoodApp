package com.phoenix.tech.foodapp.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView


@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    // Always force light theme color scheme
    val colorScheme = lightScheme
    val view = LocalView.current

    val activity = LocalActivity.current as? ComponentActivity

    if (!view.isInEditMode && activity != null) {
        DisposableEffect(Unit) {
            // Force light status and navigation bars (dark icons)
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                ),
                navigationBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                )
            )
            onDispose {}
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

//@Composable
//fun AppTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    content: @Composable () -> Unit
//) {
//    val colorScheme = if (darkTheme) darkScheme else lightScheme
//    val view = LocalView.current
//
//    // Use LocalActivity instead of casting LocalContext
//    val activity = LocalActivity.current as? ComponentActivity
//
//    if (!view.isInEditMode && activity != null) {
//        DisposableEffect(darkTheme) {
//            activity.enableEdgeToEdge(
//                statusBarStyle = SystemBarStyle.auto(
//                    android.graphics.Color.TRANSPARENT,
//                    android.graphics.Color.TRANSPARENT,
//                ) { darkTheme },
//                navigationBarStyle = SystemBarStyle.auto(
//                    android.graphics.Color.TRANSPARENT,
//                    android.graphics.Color.TRANSPARENT,
//                ) { darkTheme }
//            )
//            onDispose {}
//        }
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = AppTypography,
//        content = content
//    )
//}