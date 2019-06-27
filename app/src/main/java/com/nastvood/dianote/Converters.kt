package com.nastvood.dianote

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(value),
            ZoneId.of("UTC").normalized()
        )
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime): Long {
        return date.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @TypeConverter
    fun toNoteType(value: Byte): NoteType {
        if (value == NoteType.RAPID.value) {
           return NoteType.RAPID
        } else {
           return NoteType.LONG
        }
    }

    @TypeConverter
    fun fromNoteType(type: NoteType):Byte {
        return type.value
    }
}