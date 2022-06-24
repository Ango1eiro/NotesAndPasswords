package com.myfirstcompose.notesandpasswords

enum class NotesAndPasswordsListScreen {
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