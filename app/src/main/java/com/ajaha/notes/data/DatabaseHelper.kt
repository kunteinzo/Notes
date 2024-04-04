package com.ajaha.notes.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "database.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "create table Notes (" +
                    "id integer primary key autoincrement," +
                    "title varchar," +
                    "content text," +
                    "created text," +
                    "edited text" +
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun addNote(title: String, content: String, currentDateTime: String) {
        writableDatabase.insert("Notes", null, ContentValues().apply {
            put("title", title)
            put("content", content)
            put("created", currentDateTime)
            put("edited", currentDateTime)
        })
    }

    fun getNotes(): ArrayList<NoteModel> {
        val cursor = readableDatabase.rawQuery("select * from Notes;", null)
        val notesList = ArrayList<NoteModel>()
        if (cursor.moveToFirst()) {
            do {
                notesList.add(
                    NoteModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notesList
    }

    fun updateNote(id: Int, title: String, content: String, edited: String) {
        writableDatabase.update("Notes", ContentValues().apply {
            put("title", title)
            put("content", content)
            put("edited", edited)
        }, "id=$id", null)
    }

    fun deleteNote(id: Int) {
        writableDatabase.delete("Notes", "id=$id", null)
    }
}