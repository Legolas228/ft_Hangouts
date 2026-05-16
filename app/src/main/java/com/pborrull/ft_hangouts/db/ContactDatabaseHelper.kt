package com.pborrull.ft_hangouts.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ftHangouts.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE contacts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT,
                address TEXT,
                note TEXT,
                photo_uri TEXT
            );
        """.trimIndent()

        val CREATE_MESSAGES_TABLE = """
            CREATE TABLE messages(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                contact_id INTEGER,
                body TEXT,
                timestamp INTEGER,
                is_sent INTEGER
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
        db.execSQL(CREATE_MESSAGES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS contacts")
        db.execSQL("DROP TABLE IF EXISTS messages")
        onCreate(db)
    }
}
