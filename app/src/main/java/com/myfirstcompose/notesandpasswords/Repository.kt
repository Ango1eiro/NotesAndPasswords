package com.myfirstcompose.notesandpasswords

import androidx.lifecycle.LiveData
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.Tag
import com.myfirstcompose.notesandpasswords.room.DataBaseFullNap
import com.myfirstcompose.notesandpasswords.room.DataBaseNap
import com.myfirstcompose.notesandpasswords.room.DataBaseTag
import com.myfirstcompose.notesandpasswords.room.NotesAndPasswordsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NapRepository(private val napDao: NotesAndPasswordsDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun getAllDatabaseNaps(): Flow<List<DataBaseNap>> {
        return napDao.getAllNaps()
    }

    fun getDataBaseNapById(id: Long): DataBaseFullNap {
        return napDao.getFullNap(id)
    }

    fun saveNap(nap: Nap) {
        coroutineScope.launch(Dispatchers.IO) {
            napDao.insertNap(nap)
        }
    }

    fun deleteNap(id: Long) {
        coroutineScope.launch(Dispatchers.IO) {
            napDao.deleteNap(id)
        }
    }

    fun getAllTags() : Flow<List<DataBaseTag>> {
        return napDao.getAllTags()
    }

    fun newTag(newTagName: String) {
        coroutineScope.launch(Dispatchers.IO) {
            napDao.newTag(DataBaseTag(id = 0, name = newTagName))
        }
    }

}