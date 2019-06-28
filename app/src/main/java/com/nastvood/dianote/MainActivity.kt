package com.nastvood.dianote

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DialogAddNote.NoticeDialogListener, CalendarFragment.OnFragmentInteractionListener {

    lateinit var db: AppDatabase
    val notesLimit: Int = 100
    lateinit var navController: NavController

    fun getTable(): TableLayout {
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
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, resources.getString(R.string.app_name))
            .allowMainThreadQueries()
            .build()

        val btnLong:Button = findViewById(R.id.btn_long)
        btnLong.setOnClickListener {
            val dialog = DialogAddNote(NoteType.LONG, emptyList())
            dialog.show(supportFragmentManager, NoteType.LONG.name)
        }


        val btnRapid:Button = findViewById(R.id.btn_rapid)
        btnRapid.setOnClickListener {
            val dialog = DialogAddNote(NoteType.RAPID, listOf(1, 2, 3, 4))
            dialog.show(supportFragmentManager, NoteType.RAPID.name)
        }

        firstFillTable()
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_notes -> {

            }
            R.id.nav_calendar -> {
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDialogOkClick(dialog: DialogAddNote) {

        var note = Note(null, dialog.noteType, LocalDateTime.now(), dialog.value!!)
        note.uid = db.noteDao().insert(note)
        addNote(note)

        Toast.makeText(this, R.string.add_success, Toast.LENGTH_SHORT).show()

        Log.v("click", "%d %s uid %d".format(dialog.value, dialog.noteType.name, note.uid))
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
