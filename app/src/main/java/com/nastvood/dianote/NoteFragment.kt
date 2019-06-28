package com.nastvood.dianote

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentTransaction
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
    val notesLimit: Int = 100
    val maxCountPreload = 5
    private lateinit var db:AppDatabase

    fun addNote(note: Note) {
        val padding = 10
        val paddingRow = 5
        val timeString = note.date.format(DateTimeFormatter.ofPattern("H:m"));
        val dt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(resources.configuration.locales[0])
        val dateString= note.date.format(dt)

        val row = TableRow(this.context)
        row.setPadding(paddingRow)
        row.isClickable = true

        row.setOnLongClickListener {
            Log.v("tag", "%d".format(note.uid))
            AlertDialog.Builder(this.context!!)
                .setTitle(R.string.delete_question)
                .setMessage("%s %s %s %d".format(note.type.name, note.date.format(dt), timeString, note.amount))
                .setPositiveButton(R.string.yes) { _, _ ->
                    db.noteDao().delete(note)
                    table.removeView(row)
                    Toast.makeText(this.context, R.string.delete_success, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.no, null)
                .show()

            true
        }

        row.setOnClickListener {
            val dialog = DialogEditNote(note)
            dialog.setTargetFragment(this, 0)
            dialog.show(fragmentManager!!, NoteType.LONG.name)

            true
        }

        val labelType = TextView(this.context)
        labelType.setTypeface(null, Typeface.BOLD)
        labelType.setPadding(padding)
        labelType.text = note.type.name
        labelType.setBackgroundColor(Color.argb(50, 200, 200, 200))
        labelType.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(labelType)

        val labelDate = TextView(this.context)
        labelDate.text = dateString
        labelDate.setPadding(padding)
        labelDate.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(labelDate)

        val labelTime = TextView(this.context)
        labelTime.text = timeString
        labelTime.setPadding(padding)
        labelTime.gravity = Gravity.CENTER_HORIZONTAL
        row.addView(labelTime)


        val labelAmount = TextView(this.context)
        labelAmount.text = note.amount.toString()
        labelAmount.setPadding(padding)
        labelAmount.gravity = Gravity.CENTER_HORIZONTAL
        labelAmount.setTypeface(null, Typeface.BOLD)
        row.addView(labelAmount)

        table.addView(row)
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

    override fun onDialogOkEditClick(dialog: DialogEditNote) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            NoteFragment().apply {
            }
    }
}
