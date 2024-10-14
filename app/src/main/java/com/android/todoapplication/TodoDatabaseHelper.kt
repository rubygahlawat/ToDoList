package com.android.todoapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TODO_ITEM")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TODO_LIST")
        onCreate(db)
    }

    // Insert a new todo list
    fun insertTodoList(listName: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LIST_NAME, listName)
        }
        db.insert(TABLE_TODO_LIST, null, values)
        db.close()
    }

    // Retrieve all todo lists
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

    // Insert a new todo item
    fun insertTodoItem(itemName: String, dueDate: String?, listId: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ITEM_NAME, itemName)
            put(COLUMN_ITEM_DUE_DATE, dueDate)
            put(COLUMN_ITEM_LIST_ID, listId)
        }
        val result = db.insert(TABLE_TODO_ITEM, null, values)
        db.close()
        return result // Return the result of the insertion
    }

    fun getTodoItemsForList(listId: Int): List<Pair<String, String?>> {
        val todoItems = mutableListOf<Pair<String, String?>>()
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_ITEM_NAME, $COLUMN_ITEM_DUE_DATE FROM $TABLE_TODO_ITEM WHERE $COLUMN_ITEM_LIST_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(listId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME))
                val dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_DUE_DATE))
                todoItems.add(Pair(itemName, dueDate))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return todoItems
    }

    // Method to update a todo item
    fun updateTodoItem(itemId: Int, taskName: String, dueDate: String,isCompleted: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ITEM_COMPLETED, if (isCompleted) 1 else 0)
            put(COLUMN_ITEM_NAME, taskName) // Assuming COLUMN_ITEM_NAME is the name of the column for task names
            put(COLUMN_ITEM_DUE_DATE, dueDate)   // Assuming COLUMN_DUE_DATE is the name of the column for due dates
        }
        db.update(TABLE_TODO_ITEM, values, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))
        db.close()
    }


    // Method to delete a todo item
    fun deleteTodoItem(itemId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_TODO_ITEM, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))
        db.close()
    }


}