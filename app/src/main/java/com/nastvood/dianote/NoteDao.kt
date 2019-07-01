package com.nastvood.dianote

import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM note LIMIT :limit")
    fun lastNotes(limit: Int):List<Note>

    @Insert
    fun insert(note: Note): Long

    @Delete
    fun delete(note: Note)

    @Update
    fun update(note: Note)
}