package com.nastvood.dianote

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment

class DialogEditNote(note:Note) : DialogFragment() {

    var value:Byte? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    interface NoticeDialogListener {
        fun onDialogOkEditClick(dialog:DialogEditNote)
    }

    lateinit var mListener:NoticeDialogListener;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.fragment_note_edit, null)

        val sp = view.findViewById(R.id.sp_typeEditNote) as Spinner

        sp.adapter = ArrayAdapter(this.context!!, android.R.layout.simple_spinner_dropdown_item, NoteType.values().map { it.name })

        val btn_ok = view.findViewById(R.id.btn_okEditNote) as Button
        val btn_cancel = view.findViewById(R.id.btn_cancelEditNote) as Button

        btn_ok.setOnClickListener {
            //mListener.onDialogOkEditClick(this)
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