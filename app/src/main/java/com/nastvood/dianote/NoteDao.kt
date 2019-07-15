package com.nastvood.dianote

import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM note LIMIT :limit")
    fun lastNotes(limit: Int):List<Note>

    @Query("SELECT type, day, month, year, SUM(amount) as sum FROM\n" +
            "(SELECT  type, amount,\n" +
            "       CAST(strftime('%m', datetime(date/1000, 'unixepoch','localtime', 'utc')) AS INTEGER) AS month, " +
            "       CAST(strftime('%Y', datetime(date/1000, 'unixepoch','localtime', 'utc')) AS integer) AS year, " +
            "       CAST(strftime('%d', datetime(date/1000, 'unixepoch','localtime', 'utc')) AS integer) AS day " +
            "FROM Note " +
            "WHERE :start <= date AND date <= :finish)  " +
            "AS tmp GROUP BY type, day, month, year")
    fun groupNotesByDate(start:Long, finish:Long):List<NoteDay>

    @Insert
    fun insert(note: Note): Long

    @Delete
    fun delete(note: Note)

    @Update
    fun update(note: Note)
}