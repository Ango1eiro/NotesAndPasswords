package com.myfirstcompose.notesandpasswords

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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

fun getSimpleNapList() = List(20) { i ->
    SimpleNap(
        id = i.toLong(),
        title = "NAP beautiful title #$i",
        image = "")
}

//fun getPreviewNap(): Nap {
//
//    val nap = Nap(title = mutableStateOf("Test title"))
//
//    nap.notes.add(Note(title = mutableStateOf("1")))
//    nap.notes.add(Note(title = mutableStateOf("2")))
//
//    nap.credentials.add(Credential(title = "1"))
//    nap.credentials.add(Credential(title = "2"))
//
//    return nap
//}
