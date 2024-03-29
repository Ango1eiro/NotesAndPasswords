package com.myfirstcompose.notesandpasswords.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.myfirstcompose.notesandpasswords.data.Nap
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesAndPasswordsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNap(nap: DataBaseNap) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: DataBaseNote) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCredential(credential: DataBaseCredential) : Long

    @Transaction
    fun insertNap(nap: Nap)
    {
        val id = insertNap(DataBaseNap(id = nap.id, title = nap.title.value, image = nap.image, tag = nap.tag.value))
        nap.notes.forEach{
            insertNote(DataBaseNote(napId = id, id = it.id, title = it.title.value, content = it.content.value))
        }
        nap.credentials.forEach{
            insertCredential(DataBaseCredential(napId = id, id = it.id, title = it.title.value, login = it.login.value, password = it.password.value))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun newTag(tag: DataBaseTag) : Long

    @Query("DELETE FROM naps WHERE id = :id")
    fun deleteNap(id: Long)

    @Query("SELECT * FROM naps")
    fun getAllNaps(): Flow<List<DataBaseNap>>

    @Query("SELECT * FROM naps WHERE title like :searchText")
    fun getAllNapsWithSearch(searchText: String): Flow<List<DataBaseNap>>

    @Query("SELECT * FROM naps WHERE id = :id")
    fun getFullNap(id: Long) : DataBaseFullNap

    @Query("SELECT * FROM tags")
    fun getAllTags() : Flow<List<DataBaseTag>>

}