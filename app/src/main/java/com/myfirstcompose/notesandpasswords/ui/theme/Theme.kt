package com.myfirstcompose.notesandpasswords.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = PinkLight,
    primaryVariant = PinkHeavy,
    secondary = PinkLight
)

private val LightColorPalette = lightColors(
    primary = PinkMedium,
    primaryVariant = PinkHeavy,
    secondary = PinkLight
)

@Composable
fun NotesAndPasswordsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val systemUiController = rememberSystemUiController()
    if(darkTheme){
        systemUiController.setSystemBarsColor(
            color = Color.Black
        )
    }else{
        systemUiController.setSystemBarsColor(
            color = PinkHeavy
        )
    }


    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}