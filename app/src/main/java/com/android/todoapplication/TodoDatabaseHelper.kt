package com.android.todoapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TodoDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TodoApp.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_TODO_LIST = "TodoList"
        private const val COLUMN_LIST_ID = "id"
        private const val COLUMN_LIST_NAME = "name"

        private const val TABLE_TODO_ITEM = "TodoItem"
        private const val COLUMN_ITEM_ID = "id"
        private const val COLUMN_ITEM_NAME = "name"
        private const val COLUMN_ITEM_DUE_DATE = "dueDate"
        private const val COLUMN_ITEM_COMPLETED = "completed"
        private const val COLUMN_ITEM_LIST_ID = "listId"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTodoListTable = """
            CREATE TABLE $TABLE_TODO_LIST (
                $COLUMN_LIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LIST_NAME TEXT UNIQUE NOT NULL
            )
        """.trimIndent()

        val createTodoItemTable = """
            CREATE TABLE $TABLE_TODO_ITEM (
                $COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ITEM_NAME TEXT NOT NULL,
                $COLUMN_ITEM_DUE_DATE TEXT,
                $COLUMN_ITEM_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_ITEM_LIST_ID INTEGER,
                FOREIGN KEY($COLUMN_ITEM_LIST_ID) REFERENCES $TABLE_TODO_LIST($COLUMN_LIST_ID)
            )
        """.trimIndent()

        db?.execSQL(createTodoListTable)
        db?.execSQL(createTodoItemTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TODO_LIST")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TODO_ITEM")
        onCreate(db)
    }

    fun insertTask(taskText: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_LIST_NAME, taskText)
        db.insert(TABLE_TODO_LIST, null, values)
        db.close()
    }

    fun getAllTodoLists(): List<String> {
        val todoLists = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_LIST_NAME FROM $TABLE_TODO_LIST"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val listName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_NAME))
                todoLists.add(listName)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return todoLists
    }

    // Other database methods for managing tasks, lists, and updates
}
