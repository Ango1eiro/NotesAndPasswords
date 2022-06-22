package com.myfirstcompose.notesandpasswords

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.room.DataBaseFullNap
import com.myfirstcompose.notesandpasswords.room.DataBaseNap
import com.myfirstcompose.notesandpasswords.room.NotesAndPasswordsDao
import kotlinx.coroutines.*

class NapRepository(private val napDao: NotesAndPasswordsDao) {

    val allNaps: LiveData<List<DataBaseNap>> = napDao.getAllNaps()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

//    val currentNap = MutableLiveData<DataBaseFullNap>()

    fun getDataBaseNapById(id: Long) : DataBaseFullNap {
        return napDao.getFullNap(id)
    }

//    fun initialiseCurrentNap() {
//        currentNap.value = DataBaseFullNap()
//    }

    fun saveNap(nap: Nap) {
        coroutineScope.launch(Dispatchers.IO) {
            napDao.insertNap(nap)
        }
    }


//        private fun asyncFind(id: Long): Deferred<DataBaseFullNap?> =
//        coroutineScope.async(Dispatchers.IO) {
//            return@async napDao.getFullNap(id)
//        }

//
//    val searchResults = MutableLiveData<List<Product>>()

//

//
    fun deleteNap(id: Long) {
        coroutineScope.launch(Dispatchers.IO) {
            napDao.deleteNap(id)
        }
    }
//
//    fun findProduct(name: String) {
//        coroutineScope.launch(Dispatchers.Main) {
//            searchResults.value = asyncFind(name).await()
//        }
//    }
//

}