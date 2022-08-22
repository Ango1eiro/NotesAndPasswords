package com.myfirstcompose.notesandpasswords.utils

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.myfirstcompose.notesandpasswords.R
import com.myfirstcompose.notesandpasswords.data.Credential
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.Note
import com.myfirstcompose.notesandpasswords.data.SimpleNap

fun getImageIdByNumber(number:Long) : Int {
    return when(number.toInt()){
        1 -> R.drawable.image1
        2 -> R.drawable.image2
        3 -> R.drawable.image3
        4 -> R.drawable.image4
        else -> R.drawable.image5
    }
}

fun getSimpleNapList(applicationContext: Context) = List(20) { i ->
    SimpleNap(
        id = i.toLong(),
        title = applicationContext.getString(R.string.text_title_preview,i),
        image = "",
        tag = "")
}

fun getPreviewNap(): Nap {

    val nap = Nap(title = mutableStateOf("Test title"))

    nap.notes.add(Note(title = mutableStateOf("N 1")))
    nap.notes.add(Note(title = mutableStateOf("N 2")))

    nap.credentials.add(Credential(title = mutableStateOf("Cr 1")))
    nap.credentials.add(Credential(title = mutableStateOf("Cr 2")))

    return nap
}
