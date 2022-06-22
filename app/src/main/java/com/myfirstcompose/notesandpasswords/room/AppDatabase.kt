package com.myfirstcompose.notesandpasswords.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DataBaseNap::class,DataBaseNote::class,DataBaseCredential::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notesAndPasswordsDao(): NotesAndPasswordsDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "notes_and_passwords_database"
                    ).fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

}