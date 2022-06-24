package com.myfirstcompose.notesandpasswords

import androidx.compose.runtime.mutableStateOf
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
    notes = this.dataBaseNotes.map { it.toNote() }.toMutableStateList(),
    credentials = this.dataBaseCredentials.map { it.toCredential() }.toMutableStateList(),
)

fun DataBaseNote.toNote() = Note(
    id = this.id,
    title = mutableStateOf(this.title),
    content = mutableStateOf(this.content)
)

fun DataBaseCredential.toCredential() = Credential(
    id = this.id,
    title = mutableStateOf(this.title),
    login = mutableStateOf(this.login),
    password = mutableStateOf(this.password)
)