package com.ajaha.notes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.text.util.Linkify
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ajaha.notes.data.DatabaseHelper
import com.ajaha.notes.data.MainViewModel
import com.ajaha.notes.data.NoteModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var toolbar: Toolbar
    private lateinit var swipeRefreshNote: SwipeRefreshLayout
    private lateinit var recyclerNote: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        databaseHelper = DatabaseHelper(applicationContext)

        toolbar = findViewById(R.id.main_toolbar)
        swipeRefreshNote = findViewById(R.id.swipe_refresh_note)
        recyclerNote = findViewById(R.id.recycler_note)
        fabAddNote = findViewById(R.id.fab_add_note)

        setSupportActionBar(toolbar)
        swipeRefreshNote.setOnRefreshListener {
            swipeRefreshNote.isRefreshing = false
            (recyclerNote.adapter as NoteAdapter).loadNotes()
        }

        recyclerNote.setHasFixedSize(true)
        recyclerNote.layoutManager = LinearLayoutManager(applicationContext)
        recyclerNote.adapter = NoteAdapter(this)

        fabAddNote.setOnClickListener {
            startActivity(
                Intent(applicationContext, EditorActivity::class.java)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        (recyclerNote.adapter as NoteAdapter).loadNotes()
    }


    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.main_menu_settings -> {
                startActivity(
                    Intent(this, SettingsActivity::class.java)
                )
            }

            R.id.main_menu_privacy_policy -> {
                showPrivacyAndPolicy()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showPrivacyAndPolicy() {
        var content: String
        assets.open("privacy_and_policies.txt").bufferedReader().use {
            content = it.readText()
        }
        val spannableString = SpannableStringBuilder()
        for (line in content.split("\n")) {
            var edited = line + "\n"
            if (line.startsWith("#")) {
                edited = edited.removePrefix("# ")
                spannableString.append(edited)
                spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    spannableString.indexOf(edited),
                    spannableString.indexOf(edited) + edited.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                continue
            }
            spannableString.append(edited)
        }
        val privacyAndPolicy = "Privacy and Policy"
        val ssb = SpannableStringBuilder()
        ssb.append(privacyAndPolicy)
        ssb.setSpan(
            AbsoluteSizeSpan(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, 20F, resources.displayMetrics
                ).toInt()
            ), 0, privacyAndPolicy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb.setSpan(
            StyleSpan(Typeface.BOLD), 0, privacyAndPolicy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val privacyAndPolicyLayout =
            layoutInflater.inflate(R.layout.privacy_and_policy_layout, null)
        val ppDialog = AlertDialog.Builder(this).setTitle(ssb)
            //.setView(content)
            .setView(privacyAndPolicyLayout)
            .setNegativeButton("Exit") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        privacyAndPolicyLayout.findViewById<TextView>(R.id.dialog_content).apply {
            text = spannableString
            Linkify.addLinks(this, Linkify.EMAIL_ADDRESSES)
            movementMethod = LinkMovementMethod.getInstance()
        }
        ppDialog.create().show()
    }

    class NoteAdapter(private val context: Context) : RecyclerView.Adapter<NoteAdapter.NoteVH>() {

        var isEnable = false
        var isSelectAll = false
        var mainViewModel = MainViewModel()

        private val databaseHelper = DatabaseHelper(context)
        private var noteList = databaseHelper.getNotes()
        private var selectList = ArrayList<NoteModel>()

        class NoteVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val checkBox: AppCompatCheckBox = itemView.findViewById(R.id.recycler_note_check)
            val title: TextView = itemView.findViewById(R.id.recycler_note_title)
            val content: TextView = itemView.findViewById(R.id.recycler_note_content)
            val date: TextView = itemView.findViewById(R.id.recycler_note_date)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteVH {
            return NoteVH(
                LayoutInflater.from(context).inflate(R.layout.recycler_note_layout, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return noteList.size
        }

        override fun onBindViewHolder(holder: NoteVH, position: Int) {
            val note = noteList[position]
            holder.apply {
                title.text = note.title
                content.text = note.content
                date.text = note.created
            }
            holder.itemView.setOnLongClickListener {
                if (!isEnable) {
                    val actionModeCallback = object : ActionMode.Callback2() {
                        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            mode?.menuInflater?.inflate(R.menu.recycler_action_mode_menu, menu)
                            return true
                        }

                        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                            isEnable = true
                            clickItem(holder)
                            mainViewModel.get().observe(context as LifecycleOwner) {
                                mode?.title = "%s Selected".format(it)
                            }
                            return true
                        }

                        override fun onActionItemClicked(
                            mode: ActionMode?, item: MenuItem?
                        ): Boolean {
                            when (item?.itemId) {
                                R.id.recycler_menu_delete -> {
                                    for (noteModel in selectList) {
                                        databaseHelper.deleteNote(noteModel.id)
                                    }
                                    mode?.finish()
                                }

                                R.id.recycler_menu_select_all -> {
                                    if (selectList.size < noteList.size) {
                                        isSelectAll = true
                                        selectList.clear()
                                        selectList.addAll(noteList)
                                        mainViewModel.set(selectList.size.toString())
                                        loadNotes()
                                    }
                                }

                                R.id.recycler_menu_deselect -> {
                                    isSelectAll = false
                                    selectList.clear()
                                    mainViewModel.set(selectList.size.toString())
                                    loadNotes()
                                }
                            }
                            return true
                        }

                        override fun onDestroyActionMode(mode: ActionMode?) {
                            isEnable = false
                            isSelectAll = false
                            selectList.clear()
                            loadNotes()
                        }
                    }
                    holder.itemView.startActionMode(actionModeCallback)
                } else {
                    clickItem(holder)
                }
                true
            }
            holder.itemView.setOnClickListener {
                if (isEnable) {
                    clickItem(holder)
                } else {
                    context.startActivity(
                        Intent(context, EditorActivity::class.java).putExtra(
                            "id", note.id
                        )
                    )
                }
            }

            if (isSelectAll) {
                holder.checkBox.visibility = View.VISIBLE
            } else {
                holder.checkBox.visibility = View.GONE
            }
        }

        private fun clickItem(holder: NoteVH) {
            val noteModel = noteList[holder.adapterPosition]
            if (holder.checkBox.isVisible) {
                holder.checkBox.visibility = View.GONE
                selectList.remove(noteModel)
            } else {
                holder.checkBox.visibility = View.VISIBLE
                selectList.add(noteModel)
            }
            mainViewModel.set(selectList.size.toString())
        }

        @SuppressLint("NotifyDataSetChanged")
        fun loadNotes() {
            noteList = databaseHelper.getNotes()
            notifyDataSetChanged()
        }
    }
}