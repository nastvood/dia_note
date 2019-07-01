package com.nastvood.dianote

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DialogEditNote(val note:Note, val rowIndex:Int) : DialogFragment() {

    var value:Byte? = null
    val types = NoteType.values().map { it.name }
    lateinit var sp:Spinner
    lateinit var dp:DatePicker
    lateinit var tp:TimePicker
    lateinit var np: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    interface NoticeDialogListener {
        fun onDialogOkEditClick(note: Note, rowIndex: Int)
    }

    lateinit var mListener:NoticeDialogListener;

    fun getUpdatedNote():Note {
        val typeName = types[sp.selectedItemId.toInt()]
        val date = LocalDateTime.of(dp.year, dp.month, dp.dayOfMonth, tp.hour, tp.minute)
        return Note(note.uid, NoteType.valueOf(typeName), date, np.value.toByte())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.fragment_note_edit, null)

        sp = view.findViewById(R.id.sp_typeEditNote)
        sp.apply {
            adapter = ArrayAdapter (this.context!!, android.R.layout.simple_spinner_dropdown_item, types)
            setSelection(types.indexOf(note.type.name))
        }

        dp = view.findViewById(R.id.dp_EditNote)
        dp.updateDate(note.date.year, note.date.month.value - 1, note.date.dayOfMonth)

        tp = view.findViewById(R.id.tp_EditNote)
        tp.apply {
            hour = note.date.hour
            minute = note.date.minute
            setIs24HourView(android.text.format.DateFormat.is24HourFormat(this.context))
        }

        np = view.findViewById(R.id.np_AmountEditNote)
        np.apply {
            minValue = 1
            maxValue = Byte.MAX_VALUE.toInt()
            value = note.amount.toInt()
        }

        val btn_ok = view.findViewById(R.id.btn_okEditNote) as Button
        val btn_cancel = view.findViewById(R.id.btn_cancelEditNote) as Button

        btn_ok.setOnClickListener {
            mListener.onDialogOkEditClick(getUpdatedNote(), rowIndex)
            dismiss()
        }

        btn_cancel.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(activity)
            .setView(view)
            .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mListener = targetFragment as NoticeDialogListener
    }

}