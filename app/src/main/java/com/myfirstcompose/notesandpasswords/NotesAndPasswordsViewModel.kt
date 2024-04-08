package com.myfirstcompose.notesandpasswords

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.*
import com.myfirstcompose.notesandpasswords.data.ChipTag
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.SimpleNap
import com.myfirstcompose.notesandpasswords.data.Tag
import com.myfirstcompose.notesandpasswords.prefsstore.NAP_LIST_TYPE
import com.myfirstcompose.notesandpasswords.prefsstore.dataStore
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
    private val dataStore: DataStore<Preferences>

    private val _searchText = MutableLiveData("")

    private val _selectedTags = MutableStateFlow(emptyList<String>())
    val selectedTags: StateFlow<List<String>> = _selectedTags.asStateFlow()

    private val _filterState = MutableStateFlow(false)
    val filterState: StateFlow<Boolean> = _filterState.asStateFlow()

//    private val _napListType = MutableStateFlow(NapListType.VerticalList)
//    val napListType: StateFlow<NapListType> = _napListType.asStateFlow()

    val napListType: StateFlow<NapListType>

    val tags: LiveData<List<Tag>>

    init {
        val napDb = AppDatabase.getInstance(application)
        val napDao = napDb.notesAndPasswordsDao()
        repository = NapRepository(napDao)
        dataStore = application.applicationContext.dataStore

        napListType = dataStore.data.map { preferences ->
            if (preferences[NAP_LIST_TYPE] == null) {
                NapListType.VerticalList
            } else {
                NapListType.valueOf(preferences[NAP_LIST_TYPE]!!)
            }
        }.stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = NapListType.VerticalList)

        tags = repository.getAllTags().asLiveData().map { data ->
            data.map { it.toTag() }
        }


        allDataBaseNaps = repository.getAllDatabaseNaps()
            .combine(_searchText.asFlow()){ mDatabaseNaps, mSearchText ->
                mDatabaseNaps.filter { it.title.contains(mSearchText,true) }
            }
            .combine(_selectedTags) { mDatabaseNaps, mSelectedTags ->
                return@combine if (mSelectedTags.isNotEmpty())
                    mDatabaseNaps.filter { mSelectedTags.contains(it.tag) }
                else mDatabaseNaps
            }
            .asLiveData()
        allNaps = allDataBaseNaps.map { data ->
            data.map {
                SimpleNap(id = it.id,
                    title = it.title,
                    image = it.image,
                    tag = it.tag)
            }
        }
    }

    fun switchNapListType() {
//        if (napListType.value == NapListType.VerticalList){
//            _napListType.update { NapListType.Grid }
//        }  else  {
//            _napListType.update { NapListType.VerticalList }
//        }
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[NAP_LIST_TYPE] = if (napListType.value == NapListType.VerticalList) {
                    NapListType.Grid.name

                } else {
                    NapListType.VerticalList.name
                }
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

    fun setSearchText(newSearchText: String) {
        _searchText.postValue(newSearchText)
    }

    fun newTag(newTagName: String) {
        repository.newTag(newTagName)
    }

    fun addTagToSelection(tagName: String) {
        if (!_selectedTags.value.contains(tagName))
            _selectedTags.update { it.plus(tagName) }
    }

    fun removeTagFromSelection(tagName: String) {
        if (_selectedTags.value.contains(tagName))
            _selectedTags.update { it.minus(tagName) }
    }

    fun setFilterState(newState: Boolean) {
        _filterState.update { newState }
        if (!newState) {
            _selectedTags.update { emptyList() }
        }
    }

    fun invertFilterState() {
        setFilterState(!_filterState.value)
    }

}

enum class NapListType {
    VerticalList,Grid
}
