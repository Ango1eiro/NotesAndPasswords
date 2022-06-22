package com.myfirstcompose.notesandpasswords

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.myfirstcompose.notesandpasswords.data.Credential
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.Note
import com.myfirstcompose.notesandpasswords.room.DataBaseCredential
import com.myfirstcompose.notesandpasswords.room.DataBaseFullNap
import com.myfirstcompose.notesandpasswords.room.DataBaseNote

fun DataBaseFullNap.toNap() = Nap(
    id = this.dataBaseNap.id,
    title = mutableStateOf(this.dataBaseNap.title),
    image = this.dataBaseNap.image,
//    notes = mutableStateListOf(this.dataBaseNotes.map { it.toNote() }),
    notes = this.dataBaseNotes.map { it.toNote() }.toMutableStateList(),
    credentials = this.dataBaseCredentials.map { it.toCredential() }.toMutableStateList(),
)

fun DataBaseNote.toNote() = Note(
    id = this.id,
    title = this.title,
    content = this.content
)

fun DataBaseCredential.toCredential() = Credential(
    id = this.id,
    title = this.title,
    login = this.login,
    password = this.password
)