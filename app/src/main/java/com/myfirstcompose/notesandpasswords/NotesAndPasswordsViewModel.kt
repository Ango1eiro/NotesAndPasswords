package com.myfirstcompose.notesandpasswords

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.SimpleNap
import com.myfirstcompose.notesandpasswords.room.AppDatabase
import com.myfirstcompose.notesandpasswords.room.DataBaseNap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotesAndPasswordsViewModel(application: Application) : ViewModel() {

    private val allDataBaseNaps: LiveData<List<DataBaseNap>>
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
        allNaps = Transformations.map(allDataBaseNaps) { data ->
            data.map {
                SimpleNap(id = it.id,
                    title = it.title,
                    image = it.image)
            }
        }
    }

    fun saveNap(nap: Nap) {
        Log.v("VM", "Before nap save $nap")
        repository.saveNap(nap)
    }

    fun deleteNap(id: Long) {
        repository.deleteNap(id)
    }

    suspend fun setCurrentNap(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.v("VM", "Before _currentNap update")
            _currentNap.postValue(repository.getDataBaseNapById(id).toNap())
            Log.v("VM", "After _currentNap update")
        }.join()

    }

    fun newCurrentNap() {
        _currentNap.value = Nap()
    }

    fun resetCurrentNap() {
        _currentNap.value = null
        Log.v("VM", "After _currentNap reset")
    }

}
