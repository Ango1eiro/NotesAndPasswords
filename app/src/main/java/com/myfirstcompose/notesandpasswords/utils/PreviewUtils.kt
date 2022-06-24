package com.myfirstcompose.notesandpasswords.utils

import android.content.Context
import com.myfirstcompose.notesandpasswords.R
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
