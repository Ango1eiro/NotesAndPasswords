package com.myfirstcompose.notesandpasswords.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class Nap(
    val id: Long = 0,
    var title: MutableState<String> = mutableStateOf(""),
    var image: String = "",
    val notes: SnapshotStateList<Note> = mutableStateListOf(),
    val credentials: SnapshotStateList<Credential> = mutableStateListOf()
)

data class SimpleNap (
    val id: Long,
    var title: String,
    val image: String
)

data class Note (
    val id: Long = 0,
    var title: MutableState<String> = mutableStateOf(""),
    var content: MutableState<String> = mutableStateOf("")
)

data class Credential (
    val id: Long = 0,
    var title: MutableState<String> = mutableStateOf(""),
    var login: MutableState<String> = mutableStateOf(""),
    var password: MutableState<String> = mutableStateOf("")
)

enum class NotesAndPasswordsFolder {
    Work, Personal
}