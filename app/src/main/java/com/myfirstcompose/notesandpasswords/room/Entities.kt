package com.myfirstcompose.notesandpasswords.room

import androidx.annotation.NonNull
import androidx.room.*

@Entity(tableName = "naps")
class DataBaseNap {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "title")
    var title: String = ""

    @ColumnInfo(name = "image")
    var image: String = ""

    constructor(id: Long, title: String, image: String) {
        this.id = id
        this.title = title
        this.image = image
    }

}

data class DataBaseFullNap (
    @Embedded
    var dataBaseNap: DataBaseNap = DataBaseNap(0,"",""),
    @Relation(parentColumn = "id", entityColumn = "napId")
    var dataBaseNotes: List<DataBaseNote> = listOf(),
    @Relation(parentColumn = "id", entityColumn = "napId")
    var dataBaseCredentials: List<DataBaseCredential> = listOf()
)

@Entity(tableName = "notes", foreignKeys = [ForeignKey(
    entity = DataBaseNap::class,
    parentColumns = ["id"],
    childColumns = ["napId"],
    onDelete = ForeignKey.CASCADE
)])
class DataBaseNote {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "napId")
    var napId: Long = 0

    @ColumnInfo(name = "title")
    var title: String = ""

    @ColumnInfo(name = "content")
    var content: String = ""

    constructor(id: Long, napId: Long, title: String, content: String) {
        this.id = id
        this.napId = napId
        this.title = title
        this.content = content
    }

}

@Entity(tableName = "credentials", foreignKeys = [ForeignKey(
    entity = DataBaseNap::class,
    parentColumns = ["id"],
    childColumns = ["napId"],
    onDelete = ForeignKey.CASCADE
)])
class DataBaseCredential {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    var id: Long = 0

    @ColumnInfo(name = "napId")
    var napId: Long = 0

    @ColumnInfo(name = "title")
    var title: String = ""

    @ColumnInfo(name = "login")
    var login: String = ""

    @ColumnInfo(name = "password")
    var password: String = ""

    constructor(id: Long, napId: Long, title: String, login: String, password: String) {
        this.id = id
        this.napId = napId
        this.title = title
        this.login = login
        this.password = password
    }

}
