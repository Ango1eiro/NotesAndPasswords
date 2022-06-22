package com.myfirstcompose.notesandpasswords

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.*
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.SimpleNap
import com.myfirstcompose.notesandpasswords.room.AppDatabase
import com.myfirstcompose.notesandpasswords.room.DataBaseNap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class NotesAndPasswordsViewModel(application: Application) : ViewModel() {

    val allDataBaseNaps: LiveData<List<DataBaseNap>>
    val allNaps: LiveData<List<SimpleNap>>
    private val repository: NapRepository

    private val _currentNap = MutableLiveData<Nap>()
    val currentNap: LiveData<Nap>
        get() = _currentNap

    init {
        val napDb = AppDatabase.getInstance(application)
        val napDao = napDb.notesAndPasswordsDao()
        repository = NapRepository(napDao)

        allDataBaseNaps = repository.allNaps
        allNaps = Transformations.map(allDataBaseNaps){ data -> data.map { SimpleNap(id = it.id, title = it.title, image = it.image) }  }
//    searchResults = repository.searchResults
    }

    fun saveNap(nap: Nap) {
        Log.v("VM","Before nap save $nap")
        repository.saveNap(nap)
    }

//    fun processImage() {
//        if (isUriExternal(nap.image)){
//            val dir = File(context.filesDir,"images")
//            if(!dir.exists()){
//                dir.mkdir();
//            }
//            try {
//
//            } catch (e : Exception){
//                e.printStackTrace();
//            }
//        }
//    }

    fun isUriExternal(stringUri:String): Boolean {
        return (stringUri.contains("com.android.providers"))

    }
//
//    fun insertProduct(product: Product) {
//        repository.insertProduct(product)
//    }
//
//    fun findProduct(name: String) {
//        repository.findProduct(name)
//    }
//
    fun deleteNap(id: Long) {
        repository.deleteNap(id)
    }

    suspend fun setCurrentNap(id : Long) {
        viewModelScope.launch (Dispatchers.IO) {
            Log.v("VM","Before _currentNap update")
            _currentNap.postValue(repository.getDataBaseNapById(id).toNap())
            Log.v("VM","After _currentNap update")
        }.join()

    }

    fun newCurrentNap() {
        _currentNap.value = Nap()
    }

    fun updateNap(nap: Nap) {
        Log.v("VM","Before ${nap==_currentNap.value}")
        _currentNap.value = nap
        Log.v("VM","After ${nap==_currentNap.value}")
    }

    fun resetCurrentNap() {
        _currentNap.value = null
        Log.v("VM","After _currentNap reset")
    }

}

//fun getNotesAndPasswordsList() = List(20) { i -> Nap(
//    id = i.toLong(),
//    title = "NAP beautiful title #$i",
//    notes = List(5) { i ->
//        Note(
//            id = i.toLong(),
//            title = "Title #$i",
//            content = "Content #$i"
//        )},
//    credentials = List(3) { i ->
//        Credential(
//            id = i.toLong(),
//            title = "Title #$i",
//            login = "Login #$i",
//            password = "Password #$i"
//        )
//
//    }
//)

//}