package com.nastvood.dianote

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.room.*
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

enum class NoteType(
    val value: Byte
) {
    RAPID(0),
    LONG(1)
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): LocalDateTime {
        return  LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.of("UTC").normalized())
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
    fun fromNoteType(type:NoteType):Byte {
        return type.value
    }
}

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val uid: Long?,
    val type: NoteType,
    val date: LocalDateTime,
    val amount: Byte
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM note LIMIT :limit")
    fun lastNotes(limit: Int):List<Note>

    @Insert
    fun insert(note: Note): Long

    @Delete
    fun delete(note: Note)
}

@Database(entities = arrayOf(Note::class), version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao() : NoteDao
}

class MainActivity : AppCompatActivity() {

    lateinit var db: AppDatabase
    val notesLimit: Int = 100

    fun getTable():TableLayout {
       return findViewById(R.id.table_layout) as TableLayout
    }

    fun addNote(note: Note) {
        val tl = getTable()
        val padding = 10
        val paddingRow = 5
        //val now = LocalDateTime.now()

        //vla uid = db.noteDao().insert(Note(null, noteType, now, 8))

        val row = TableRow(this)
        row.setPadding(paddingRow)

        val label_type = TextView(this)
        label_type.setTypeface(null, Typeface.BOLD)
        label_type.setPadding(padding)
        label_type.setText(note.type.name)
        label_type.setBackgroundColor(Color.argb(50, 200, 200, 200))
        label_type.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(label_type)

        val label_date = TextView(this)
        val dt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(resources.configuration.locales[0])
        label_date.setText(note.date.format(dt))
        label_date.setPadding(padding)
        label_date.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(label_date)

        val label_time = TextView(this)
        label_time.setText(note.date.format(DateTimeFormatter.ofPattern("H:m")))
        label_time.setPadding(padding)
        label_time.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(label_time)


        val label_amount = TextView(this)
        label_amount.setText(note.amount.toString())
        label_amount.setPadding(padding)
        label_amount.gravity = Gravity.CENTER_HORIZONTAL
        label_amount.setTypeface(null, Typeface.BOLD)
        row.addView(label_amount)


        tl.addView(row)
    }

    fun firstFillTable() {
        val notes = db.noteDao().lastNotes(notesLimit)

        for (note in notes) {
            addNote(note)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, resources.getString(R.string.app_name))
            .allowMainThreadQueries()
            .build()

        btn_long.setOnClickListener {
            //addNote(NoteType.LONG)
        }

        btn_rapid.setOnClickListener {
            //addNote(NoteType.RAPID)
        }

        firstFillTable()
    }

}
