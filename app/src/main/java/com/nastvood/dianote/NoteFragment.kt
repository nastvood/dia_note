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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val LABEL_TYPE = 0
private const val LABEL_DATE = 1
private const val LABEL_TIME = 2
private const val LABEL_AMOUNT = 3

class NoteFragment :
    Fragment(),
    DialogAddNote.NoticeDialogListener,
    DialogEditNote.NoticeDialogListener {

    private var listener: OnFragmentInteractionListener? = null
    lateinit var table: TableLayout
    lateinit var sv: ScrollView
    private val notesLimit: Int = 100
    private val maxCountPreload = 5
    private lateinit var db:AppDatabase

    private val RAPID_MIN_VALUE = 3
    private val LONG_MIN_VALUE = 10

    private fun formatTimeString(date:LocalDateTime):String {
        return date.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private fun formatDate():DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(resources.configuration.locales[0])
    }

    private fun addNote(note: Note) {
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
        row.addView(labelType, LABEL_TYPE)

        val labelDate = TextView(this.context)
        labelDate.apply {
            text = dateString
            setPadding(padding)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        row.addView(labelDate, LABEL_DATE)

        val labelTime = TextView(this.context)
        labelTime.apply {
            text = timeString
            setPadding(padding)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        row.addView(labelTime, LABEL_TIME)


        val labelAmount = TextView(this.context)
        labelAmount.apply {
            text = note.amount.toString()
            setPadding(padding)
            gravity = Gravity.CENTER_HORIZONTAL
            setTypeface(null, Typeface.BOLD)
        }
        row.addView(labelAmount, LABEL_AMOUNT)

        table.addView(row)
        val childIndex = table.indexOfChild(row)

        row.setOnClickListener {
            val dialog = DialogEditNote.newInstance(note, childIndex)
            dialog.setTargetFragment(this, 0)
            dialog.show(fragmentManager!!, NoteType.LONG.name)
        }

        this.view?.post {
            sv.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun firstFillTable() {
        val notes = db.noteDao().lastNotes(notesLimit)

        for (note in notes) {
            addNote(note)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db =  AppDatabase.getInstance(activity!!.applicationContext)!!

        Log.v("database path", activity!!.getDatabasePath(resources.getString(R.string.app_name)).absolutePath)
    }

    private fun isSuitedPreloadNote(noteType: NoteType, amount: Byte): Boolean {
        return ((amount >= RAPID_MIN_VALUE && noteType == NoteType.RAPID) ||
            (amount >= LONG_MIN_VALUE && noteType == NoteType.LONG))
    }

    private fun preloadNotes(noteType: NoteType): ByteArray {
        val settings = this.context!!.getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        return settings.getString(noteType.name, null)
            ?.split(',')
            ?.map { it.toByte() }
            ?.filter { isSuitedPreloadNote(noteType, it) }
            ?.toByteArray()
            ?: ByteArray(0)
    }

    private fun updatePreloadNotes(noteType: NoteType, amount: Byte) {
        if (isSuitedPreloadNote(noteType, amount)) {
            val settings = this.context!!.getSharedPreferences(
                resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
            )
            val preload = settings.getString(noteType.name, null)?.split(',')?.map { it.toByte() }
                ?: emptyList()
            val newPreload =
                (listOf(amount) + preload.filter { it != amount }).take(this.maxCountPreload)
                    .joinToString(",")
            settings.edit()!!.apply {
                putString(noteType.name, newPreload)
                apply()
            }
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
            val dialog = DialogAddNote.newInstance(NoteType.LONG, preloadNotes(NoteType.LONG))
            dialog.setTargetFragment(this, 0)
            dialog.show(fragmentManager!!, NoteType.LONG.name)
        }

        val btnRapid: Button = view.findViewById(R.id.btn_rapid)
        btnRapid.setOnClickListener {
            val dialog = DialogAddNote.newInstance(NoteType.RAPID, preloadNotes(NoteType.RAPID))
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
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
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

        (row.get(LABEL_TYPE) as TextView).setText(note.type.name)
        (row.get(LABEL_DATE) as TextView).setText(note.date.format(formatDate()))
        (row.get(LABEL_TIME) as TextView).setText(formatTimeString(note.date))
        (row.get(LABEL_AMOUNT) as TextView).setText(note.amount.toString())
    }
}
