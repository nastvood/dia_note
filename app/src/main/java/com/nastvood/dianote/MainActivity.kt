package com.nastvood.dianote

import android.content.DialogInterface
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
    @PrimaryKey(autoGenerate = true) var uid: Long?,
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

class MainActivity : AppCompatActivity(), DialogAddNote.NoticeDialogListener {

    lateinit var db: AppDatabase
    val notesLimit: Int = 100

    fun getTable():TableLayout {
       return findViewById(R.id.table_layout) as TableLayout
    }

    fun addNote(note: Note) {
        val tl = getTable()
        val padding = 10
        val paddingRow = 5
        val timeString = note.date.format(DateTimeFormatter.ofPattern("H:m"));
        val dt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(resources.configuration.locales[0])
        val dateString= note.date.format(dt)

        val row = TableRow(this)
        row.setPadding(paddingRow)
        row.isClickable = true

        row.setOnLongClickListener {
            Log.v("tag", "%d".format(note.uid))
            AlertDialog.Builder(this)
                .setTitle(R.string.delete_question)
                .setMessage("%s %s %s %d".format(note.type.name, note.date.format(dt), timeString, note.amount))
                .setPositiveButton(R.string.yes) { _, _ ->
                    db.noteDao().delete(note)
                    tl.removeView(row)
                    Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.no, null)
                .show()

            true
        }

        val labelType = TextView(this)
        labelType.setTypeface(null, Typeface.BOLD)
        labelType.setPadding(padding)
        labelType.text = note.type.name
        labelType.setBackgroundColor(Color.argb(50, 200, 200, 200))
        labelType.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(labelType)

        val labelDate = TextView(this)
        labelDate.text = dateString
        labelDate.setPadding(padding)
        labelDate.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(labelDate)

        val labelTime = TextView(this)
        labelTime.text = timeString
        labelTime.setPadding(padding)
        labelTime.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(labelTime)


        val labelAmount = TextView(this)
        labelAmount.text = note.amount.toString()
        labelAmount.setPadding(padding)
        labelAmount.gravity = Gravity.CENTER_HORIZONTAL
        labelAmount.setTypeface(null, Typeface.BOLD)
        row.addView(labelAmount)


        tl.addView(row)
    }

    private fun addNote(uid:Long, noteType: NoteType, amount: Byte, localDateTime: LocalDateTime = LocalDateTime.now()) {
        addNote(Note(uid, noteType, localDateTime, amount))
    }

    private fun firstFillTable() {
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
            val dialog = DialogAddNote(NoteType.LONG, emptyList())
            dialog.show(supportFragmentManager, NoteType.LONG.name)
        }

        btn_rapid.setOnClickListener {
            val dialog = DialogAddNote(NoteType.RAPID, listOf(1, 2, 3, 4))
            dialog.show(supportFragmentManager, NoteType.RAPID.name)
        }

        firstFillTable()
    }

    override fun onDialogOkClick(dialog: DialogAddNote) {

        var note = Note(null, dialog.noteType, LocalDateTime.now(), dialog.value!!)
        note.uid = db.noteDao().insert(note)
        addNote(note)

        Toast.makeText(this, R.string.add_success, Toast.LENGTH_SHORT).show()

        Log.v("click", "%d %s uid %d".format(dialog.value, dialog.noteType.name, note.uid))
    }

}
