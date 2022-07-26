package com.myfirstcompose.notesandpasswords

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.*
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.SimpleNap
import com.myfirstcompose.notesandpasswords.room.AppDatabase
import com.myfirstcompose.notesandpasswords.room.DataBaseNap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesAndPasswordsViewModel(application: Application) : ViewModel() {

    private val allDataBaseNaps: LiveData<List<DataBaseNap>>
    val allNaps: LiveData<List<SimpleNap>>
    private val repository: NapRepository

//    private val _currentNap = MutableLiveData<Nap?>()
//    val currentNap: LiveData<Nap?>
//        get() = _currentNap

    private val _searchText = MutableLiveData("")
    val searchText: LiveData<String>
        get() = _searchText

    private val _napListType = MutableStateFlow(NapListType.VerticalList)
    val napListType: StateFlow<NapListType> = _napListType.asStateFlow()

    init {
        val napDb = AppDatabase.getInstance(application)
        val napDao = napDb.notesAndPasswordsDao()
        repository = NapRepository(napDao)

//        allDataBaseNaps = repository.allNaps
        allDataBaseNaps = repository.getAllDatabaseNaps()
            .combine(_searchText.asFlow()){ mDatabaseNaps, mSearchText ->
                mDatabaseNaps.filter { it.title.contains(mSearchText,true) }
            }.asLiveData()
        allNaps = Transformations.map(allDataBaseNaps) { data ->
            data.map {
                SimpleNap(id = it.id,
                    title = it.title,
                    image = it.image)
            }
        }
    }

    fun switchNapListType() {
        if (napListType.value == NapListType.VerticalList){
            _napListType.update { NapListType.Grid }
        }  else  {
            _napListType.update { NapListType.VerticalList }
        }
    }

    fun saveNap(nap: Nap) {
        Log.v("VM", "Before nap save $nap")
        repository.saveNap(nap)
    }

    fun deleteNap(id: Long) {
        repository.deleteNap(id)
    }

//    suspend fun setCurrentNap(id: Long) {
//        viewModelScope.launch(Dispatchers.IO) {
//            Log.v("VM", "Before _currentNap update")
//            _currentNap.postValue(repository.getDataBaseNapById(id).toNap())
//            Log.v("VM", "After _currentNap update")
//        }.join()
//
//    }

    suspend fun getNapById(id: Long) : Nap {
        return withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            repository.getDataBaseNapById(id).toNap()
        }
    }

    suspend fun getNewOrExistingNapById(id: Long) : Nap {
        return if (id < 0) {
            Nap()
        } else {
            getNapById(id)
        }
    }

//    fun resetCurrentNap() {
//        _currentNap.value = null
//        Log.v("VM", "After _currentNap reset")
//    }

    fun setSearchText(newSearchText: String) {
        _searchText.postValue(newSearchText)
    }

}

enum class NapListType {
    VerticalList,Grid
}
