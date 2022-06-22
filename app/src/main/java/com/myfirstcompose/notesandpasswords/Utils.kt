package com.myfirstcompose.notesandpasswords

import kotlin.random.Random

fun getImageIdByNumber(number:Long) : Int {
    return when(number.toInt()){
        1 -> R.drawable.image1
        2 -> R.drawable.image2
        3 -> R.drawable.image3
        4 -> R.drawable.image4
        else -> R.drawable.image5
    }
}