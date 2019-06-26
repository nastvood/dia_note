package com.nastvood.diashed

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

enum class NoteType(
    val str: String
) {
    RAPID("R"),
    LONG("L")
}

class MainActivity : AppCompatActivity() {

    fun getTable():TableLayout {
       return findViewById(R.id.table_layout) as TableLayout
    }

    fun addNote(noteType: NoteType) {
        val tl = getTable()

        val row = TableRow(this)

        val label_type = TextView(this)
        label_type.setTypeface(null, Typeface.BOLD)
        label_type.setPadding(10)
        label_type.setText(noteType.str)
        label_type.setBackgroundColor(Color.argb(50, 200, 200, 200))
        row.addView(label_type)

        val localDateTime = LocalDateTime.now();
        val label_date = TextView(this)
        val dt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(resources.configuration.locales[0])
        label_date.setText(localDateTime.format(dt))
        label_date.setPadding(10)
        row.addView(label_date)

        val label_time = TextView(this)
        label_time.setText(localDateTime.format(DateTimeFormatter.ofPattern("H:m")))
        label_time.setPadding(10)
        row.addView(label_time)


        val label_amount = TextView(this)
        label_amount.setText("8")
        label_amount.setPadding(10)
        row.addView(label_amount)


        tl.addView(row)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_long.setOnClickListener {
            addNote(NoteType.LONG)
        }

        btn_rapid.setOnClickListener {
            addNote(NoteType.RAPID)
        }
    }

}
