package com.nastvood.dianote

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.core.view.setPadding
import androidx.room.Room
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class NoteFragment :
    Fragment(),
    DialogAddNote.NoticeDialogListener,
    DialogEditNote.NoticeDialogListener {

    private var listener: OnFragmentInteractionListener? = null
    lateinit var table: TableLayout
    lateinit var sv: ScrollView
    val notesLimit: Int = 100
    val maxCountPreload = 5
    private lateinit var db:AppDatabase

    val label_type = 0
    val label_date = 1
    val label_time = 2
    val label_amount = 3

    fun formatTimeString(date:LocalDateTime):String {
        return date.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    fun formatDate():DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(resources.configuration.locales[0])
    }

    fun addNote(note: Note) {
        val padding = 10
        val paddingRow = 5
        val timeString = formatTimeString(note.date)
        val fd = formatDate()
        val dateString= note.date.format(fd)

        val row = TableRow(this.context)
        row.setPadding(paddingRow)
        row.isClickable = true

        row.setOnLongClickListener {
            AlertDialog.Builder(this.context!!)
                .setTitle(R.string.delete_question)
                .setMessage("%s %s %s %d".format(note.type.name, note.date.format(fd), timeString, note.amount))
                .setPositiveButton(R.string.yes) { _, _ ->
                    db.noteDao().delete(note)
                    table.removeView(row)
                    Toast.makeText(this.context, R.string.delete_success, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.no, null)
                .show()

            true
        }

        val labelType = TextView(this.context)
        labelType.apply {
            setTypeface(null, Typeface.BOLD)
            setPadding(padding)
            text = note.type.name
            setBackgroundColor(Color.argb(50, 200, 200, 200))
            gravity = Gravity.CENTER_HORIZONTAL
        }
        row.addView(labelType, label_type)

        val labelDate = TextView(this.context)
        labelDate.apply {
            text = dateString
            setPadding(padding)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        row.addView(labelDate, label_date)

        val labelTime = TextView(this.context)
        labelTime.apply {
            text = timeString
            setPadding(padding)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        row.addView(labelTime, label_time)


        val labelAmount = TextView(this.context)
        labelAmount.apply {
            text = note.amount.toString()
            setPadding(padding)
            gravity = Gravity.CENTER_HORIZONTAL
            setTypeface(null, Typeface.BOLD)
        }
        row.addView(labelAmount, label_amount)

        table.addView(row)
        val childIndex = table.indexOfChild(row)

        row.setOnClickListener {
            val dialog = DialogEditNote(note, childIndex)
            dialog.setTargetFragment(this, 0)
            dialog.show(fragmentManager!!, NoteType.LONG.name)

            true
        }
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

        db = Room.databaseBuilder(activity!!.applicationContext, AppDatabase::class.java, resources.getString(R.string.app_name))
            .allowMainThreadQueries()
            .build()

        Log.v("database path", activity!!.getDatabasePath(resources.getString(R.string.app_name)).absolutePath)
    }

    fun preloadNotes(noteType: NoteType): List<Byte> {
        val settings = this.context!!.getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        return settings.getString(noteType.name, null)?.split(',')?.map { it.toByte() } ?: emptyList()
    }

    fun updatePreloadNotes(noteType: NoteType, amount: Byte) {
        val settings =  this.context!!.getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        val preload = settings.getString(noteType.name, null)?.split(',')?.map { it.toByte() } ?: emptyList()
        val newPreload = (listOf(amount) + preload.filter { it != amount }).take(this.maxCountPreload).joinToString(",")
        settings.edit()!!.apply {
            putString(noteType.name, newPreload)
            apply()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note, container, false)

        table = view.findViewById(R.id.table_layout)
        sv = view.findViewById(R.id.sv_fragmen_note)

        firstFillTable()

        val btnLong: Button = view.findViewById(R.id.btn_long)
        btnLong.setOnClickListener {
            val dialog = DialogAddNote(NoteType.LONG, preloadNotes(NoteType.LONG))
            dialog.setTargetFragment(this, 0)
            dialog.show(fragmentManager!!, NoteType.LONG.name)
        }

        val btnRapid: Button = view.findViewById(R.id.btn_rapid)
        btnRapid.setOnClickListener {
            val dialog = DialogAddNote(NoteType.RAPID, preloadNotes(NoteType.RAPID))
            dialog.setTargetFragment(this, 0)
            dialog.show(fragmentManager!!, NoteType.RAPID.name)
        }

        view.post {
            sv.fullScroll(ScrollView.FOCUS_DOWN)
        }

        return view
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    override fun onDialogOkClick(dialog: DialogAddNote) {

        var note = Note(null, dialog.noteType, LocalDateTime.now(), dialog.value!!)
        note.uid = db.noteDao().insert(note)
        addNote(note)

        Toast.makeText(activity!!.applicationContext, R.string.add_success, Toast.LENGTH_SHORT).show()

        updatePreloadNotes(note.type, note.amount)
    }

    override fun onDialogOkEditClick(note: Note, rowIndex:Int) {
        db.noteDao().update(note)

        val row = table.getChildAt(rowIndex) as TableRow

        (row.get(label_type) as TextView).setText(note.type.name)
        (row.get(label_date) as TextView).setText(note.date.format(formatDate()))
        (row.get(label_time) as TextView).setText(formatTimeString(note.date))
        (row.get(label_amount) as TextView).setText(note.amount.toString())
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            NoteFragment().apply {
            }
    }
}
