package com.ajaha.notes

import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ajaha.notes.data.DatabaseHelper
import com.ajaha.notes.data.NoteModel
import java.util.Calendar

class EditorActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var noteModel: NoteModel
    private var currentDateTime = ""
    private lateinit var toolbar: Toolbar
    private lateinit var editorEdtTitle: EditText
    private lateinit var editorEdtContent: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.editor_toolbar)
        editorEdtTitle = findViewById(R.id.editor_edt_title)
        editorEdtContent = findViewById(R.id.editor_edt_content)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        databaseHelper = DatabaseHelper(applicationContext)
        currentDateTime =
            DateFormat.format("yyyy-MM-dd HH:mm:ss", Calendar.getInstance()).toString()
        supportActionBar?.subtitle = currentDateTime
        if (intent.hasExtra("id")) {
            noteModel = databaseHelper.getNotes().find { it.id == intent.getIntExtra("id", 0) }!!
            supportActionBar?.subtitle = noteModel.edited
            editorEdtTitle.setText(noteModel.title)
            editorEdtContent.setText(noteModel.content)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.editor_menu_save -> {
                if (intent.hasExtra("id")) {
                    databaseHelper.updateNote(
                        noteModel.id,
                        editorEdtTitle.text.toString(),
                        editorEdtContent.text.toString(),
                        currentDateTime
                    )
                } else {
                    databaseHelper.addNote(
                        editorEdtTitle.text.toString(),
                        editorEdtContent.text.toString(),
                        currentDateTime
                    )
                }

            }

            R.id.editor_menu_delete -> {
                if (intent.hasExtra("id")) {
                    databaseHelper.deleteNote(intent.getIntExtra("id", 0))
                }
            }
        }
        finish()
        return super.onOptionsItemSelected(item)
    }
}