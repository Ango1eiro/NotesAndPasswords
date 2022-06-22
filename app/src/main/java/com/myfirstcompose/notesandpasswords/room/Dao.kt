package com.myfirstcompose.notesandpasswords.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.myfirstcompose.notesandpasswords.data.Nap

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
        val id = insertNap(DataBaseNap(id = nap.id, title = nap.title.value, image = nap.image))
        nap.notes.forEach{
            insertNote(DataBaseNote(napId = id, id = it.id, title = it.title, content = it.content))
        }
        nap.credentials.forEach{
            insertCredential(DataBaseCredential(napId = id, id = it.id, title = it.title, login = it.login, password = it.password))
        }
    }

    @Query("DELETE FROM naps WHERE id = :id")
    fun deleteNap(id: Long)

    @Query("SELECT * FROM naps")
    fun getAllNaps(): LiveData<List<DataBaseNap>>

    @Query("SELECT * FROM naps WHERE id = :id")
    fun getFullNap(id: Long) : DataBaseFullNap

}