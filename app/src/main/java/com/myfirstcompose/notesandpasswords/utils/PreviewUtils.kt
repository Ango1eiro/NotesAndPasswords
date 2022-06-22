package com.myfirstcompose.notesandpasswords

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
