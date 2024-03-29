package com.nastvood.dianote

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDateTime

@Entity(indices = arrayOf(Index(value = ["date"])))
data class Note(
    @PrimaryKey(autoGenerate = true) var uid: Long?,
    val type: NoteType,
    val date: LocalDateTime,
    val amount: Byte
) : Serializable

data class NoteDay(
    val type: NoteType,
    val day: Int,
    val month: Int,
    val year: Int,
    val sum: Int
)