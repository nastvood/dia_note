package com.nastvood.dianote

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteDao {
    @Query("SELECT * FROM note LIMIT :limit")
    fun lastNotes(limit: Int):List<Note>

    @Insert
    fun insert(note: Note): Long

    @Delete
    fun delete(note: Note)
}