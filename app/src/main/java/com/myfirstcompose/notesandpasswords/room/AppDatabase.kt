package com.myfirstcompose.notesandpasswords.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.myfirstcompose.notesandpasswords.R

@Database(
    entities = [
        DataBaseNap::class,
        DataBaseNote::class,
        DataBaseCredential::class,
        DataBaseTag::class
               ],
    version = 3,
    autoMigrations = [
        AutoMigration(
            from = 2,
            to = 3,
            spec = AppDatabase.MyAutoMigrationTags::class
        )
                     ],
    exportSchema = true)
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
                        context.getString(R.string.database_name)
                    ).build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    class MyAutoMigrationTags : AutoMigrationSpec {}

}