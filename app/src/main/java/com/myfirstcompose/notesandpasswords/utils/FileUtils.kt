package com.myfirstcompose.notesandpasswords.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

fun createCopyAndReturnRealPath(context: Context, uri: Uri): Uri? {
    val contentResolver = context.contentResolver ?: return null
    val filePath: String = (context.filesDir.absolutePath + File.separator
            + uri.lastPathSegment)
    val file = File(filePath)
    try {
        file.parentFile!!.mkdirs()
        file.createNewFile()
        val inputStream = contentResolver.openInputStream(uri) ?: return null //crashing here
        val outputStream: OutputStream = FileOutputStream(file)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()
    } catch (ignore: IOException) {
        return null
    }
    Log.v("NotesAndPasswordsDetail", "Absolute path - $file.absolutePath")
    return Uri.fromFile(file)
}