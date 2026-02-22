package com.example.app.app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.app.feature.notes.data.local.NoteDao
import com.example.app.feature.notes.data.local.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
