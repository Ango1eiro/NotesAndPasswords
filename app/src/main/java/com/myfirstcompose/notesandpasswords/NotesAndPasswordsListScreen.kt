package com.myfirstcompose.notesandpasswords

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector

enum class NotesAndPasswordsListScreen(

) {
    Main, Detail;

    companion object {
        fun fromRoute(route: String?): NotesAndPasswordsListScreen =
            when (route?.substringBefore("/")) {
                Main.name -> Main
                null -> Main
                else -> throw IllegalArgumentException("Route $route is not recognized.")
            }
    }
}