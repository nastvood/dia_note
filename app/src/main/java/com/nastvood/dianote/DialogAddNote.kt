package com.nastvood.dianote

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment

private const val ARG_NOTE_TYPE = "noteType"
private const val ARG_DEFAULT_VALS = "defaultVals"

class DialogAddNote() : DialogFragment() {
    var value:Byte? = null
    lateinit var noteType:NoteType
    lateinit var defaultVals:ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            noteType = NoteType.valueOf(it.getString(ARG_NOTE_TYPE)!!)
            defaultVals = it.getByteArray(ARG_DEFAULT_VALS)!!
        }
    }

    interface NoticeDialogListener {
        fun onDialogOkClick(dialog:DialogAddNote)
    }

    lateinit var mListener:NoticeDialogListener;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.fragment_note_add, null)

        val np = view.findViewById(R.id.np_ValueAddNote) as NumberPicker
        np.minValue = 1
        np.maxValue = Byte.MAX_VALUE.toInt()

        val btn_ok = view.findViewById(R.id.btn_okAddNote) as Button
        val btn_cancel = view.findViewById(R.id.btn_cancelAddNote) as Button

        btn_ok.setOnClickListener {
            value = np.value.toByte()
            mListener.onDialogOkClick(this)
            dismiss()
        }

        btn_cancel.setOnClickListener {
            dismiss()
        }

        val ll = view.findViewById(R.id.ll) as LinearLayout
        for(value in defaultVals) {
            val button = Button(this.context)
            button.text = value.toString()
            button.setOnClickListener {
                this.value = value
                mListener.onDialogOkClick(this)
                dismiss()
            }
            ll.addView(button)
        }

        return AlertDialog.Builder(activity).setView(view).setTitle(this.noteType.name).create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mListener = targetFragment as NoticeDialogListener
    }

    companion object {
        @JvmStatic
        fun newInstance(noteType: NoteType, defaultVals: ByteArray) =
            DialogAddNote().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOTE_TYPE, noteType.name)
                    putByteArray(ARG_DEFAULT_VALS, defaultVals)

                }
            }
    }
}