package com.android.todoapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

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

    // Update a  todo list name
    fun updateListName(listId: Int, listName: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LIST_NAME, listName)
        }

        // Log the incoming parameters
        Log.d("UpdateTodoItem", "Attempting to update listName for listId: $listId with new name: $listName")

        // Perform the update operation
        val result = db.update(TABLE_TODO_LIST, values, "$COLUMN_LIST_ID = ?", arrayOf(listId.toString()))

        // Log the result of the update
        if (result != 0) {
            Log.d("UpdateTodoItem", "Update successful for listId: $listId")
        } else {
            Log.e("UpdateTodoItem", "Update failed for listId: $listId. No rows affected.")
        }

        db.close()
        return result != 0
    }
    fun getTodoListsWithCounts(): List<Map<String, Any>> {
        val todoLists = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase

        // Updated query to get the total, completed items, and nearest due date for each list
        val query = """
        SELECT 
            l.$COLUMN_LIST_ID, 
            l.$COLUMN_LIST_NAME, 
            COUNT(i.$COLUMN_ITEM_ID) AS totalItems, 
            SUM(CASE WHEN i.$COLUMN_ITEM_COMPLETED = 1 THEN 1 ELSE 0 END) AS completedItems,
            MIN(i.$COLUMN_ITEM_DUE_DATE) AS nearestDueDate
        FROM $TABLE_TODO_LIST l
        LEFT JOIN $TABLE_TODO_ITEM i ON l.$COLUMN_LIST_ID = i.$COLUMN_ITEM_LIST_ID
        GROUP BY l.$COLUMN_LIST_ID, l.$COLUMN_LIST_NAME
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val listId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIST_ID))
                val listName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_NAME))
                val totalItems = cursor.getInt(cursor.getColumnIndexOrThrow("totalItems"))
                val completedItems = cursor.getInt(cursor.getColumnIndexOrThrow("completedItems"))

                // Get the nearest due date, or set to null if there are no items
                val nearestDueDate = cursor.getString(cursor.getColumnIndexOrThrow("nearestDueDate"))
                Log.d(TAG, "nearestDueDate: $nearestDueDate")
                // Add a map for each to-do list, including the name, total, completed items, and nearest due date
                todoLists.add(
                    mapOf(
                        "listId" to listId,
                        "listName" to listName,
                        "totalItems" to totalItems,
                        "completedItems" to completedItems,
                        "dueDate" to (nearestDueDate ?: "No Due Date Added")
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return todoLists
    }


    // Retrieve all todo lists with item counts
//    fun getTodoListsWithCounts(): List<Map<String, Any>> {
//        val todoLists = mutableListOf<Map<String, Any>>()
//        val db = this.readableDatabase
//
//        // Query to get the total and completed items for each list
//        val query = """
//        SELECT
//            l.$COLUMN_LIST_ID,
//            l.$COLUMN_LIST_NAME,
//            COUNT(i.$COLUMN_ITEM_ID) AS totalItems,
//            SUM(CASE WHEN i.$COLUMN_ITEM_COMPLETED = 1 THEN 1 ELSE 0 END) AS completedItems
//        FROM $TABLE_TODO_LIST l
//        LEFT JOIN $TABLE_TODO_ITEM i ON l.$COLUMN_LIST_ID = i.$COLUMN_ITEM_LIST_ID
//        GROUP BY l.$COLUMN_LIST_ID, l.$COLUMN_LIST_NAME
//    """.trimIndent()
//
//        val cursor = db.rawQuery(query, null)
//
//        if (cursor.moveToFirst()) {
//            do {
//                val listId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIST_ID))
//                val listName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_NAME))
//                val totalItems = cursor.getInt(cursor.getColumnIndexOrThrow("totalItems"))
//                val completedItems = cursor.getInt(cursor.getColumnIndexOrThrow("completedItems"))
//
//                // Add a map for each to-do list, including the name, total, and completed items
//                todoLists.add(
//                    mapOf(
//                        "listId" to listId,
//                        "listName" to listName,
//                        "totalItems" to totalItems,
//                        "completedItems" to completedItems
//                    )
//                )
//            } while (cursor.moveToNext())
//        }
//        cursor.close()
//        return todoLists
//    }
//    @SuppressLint("Range")
//    fun getNearestDueDateForLists(): Map<Long, String?> {
//        val nearestDueDates = mutableMapOf<Long, String?>()
//
//        // SQL query to find the nearest due date for each list
//        val cursor = readableDatabase.rawQuery(
//            """
//        SELECT $COLUMN_ITEM_LIST_ID, MIN($COLUMN_ITEM_DUE_DATE) AS nearest_due_date
//        FROM $TABLE_TODO_ITEM
//        WHERE $COLUMN_ITEM_DUE_DATE IS NOT NULL
//        GROUP BY $COLUMN_ITEM_LIST_ID
//        """, null
//        )
//
//        if (cursor.moveToFirst()) {
//            do {
//                val listId = cursor.getLong(cursor.getColumnIndex(COLUMN_ITEM_LIST_ID))
//                val nearestDueDate = cursor.getString(cursor.getColumnIndex("nearest_due_date"))
//                nearestDueDates[listId] = nearestDueDate
//            } while (cursor.moveToNext())
//        }
//
//        cursor.close()
//        return nearestDueDates
//    }


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

    fun getTodoItemsForList(listId: Int): List<TodoItem> {
        val todoItems = mutableListOf<TodoItem>()
        val db = this.readableDatabase
        val query =
            "SELECT $COLUMN_ITEM_NAME, $COLUMN_ITEM_DUE_DATE, $COLUMN_ITEM_ID, $COLUMN_ITEM_COMPLETED FROM $TABLE_TODO_ITEM WHERE $COLUMN_ITEM_LIST_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(listId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val itemName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME))
                val dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_DUE_DATE))
                val taskId =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)) // Return as Int
                val isTaskCompleted =
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_COMPLETED)) == 1 // Return as Boolean (1 -> true, 0 -> false)

                todoItems.add(TodoItem(itemName, dueDate, taskId, isTaskCompleted))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close() // Close the database after reading
        return todoItems
    }


    fun updateTodoItem(itemId: Int, taskName: String, dueDate: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ITEM_NAME,taskName) // Assuming COLUMN_ITEM_NAME is the name of the column for task names
            put(COLUMN_ITEM_DUE_DATE,dueDate) // Assuming COLUMN_DUE_DATE is the name of the column for due dates
        }

        val result = db.update(TABLE_TODO_ITEM, values, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))

        // Log the result of the update
        if (result != 0) {
            Log.d("UpdateTodoItem", "Update successful for itemId: $itemId")
        } else {
            Log.e("UpdateTodoItem", "Update failed for itemId: $itemId")
        }

        db.close()
        return result != 0
    }

    fun updateCheckbox(itemId: Int, isChecked: Boolean): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(
                COLUMN_ITEM_COMPLETED,
                if (isChecked) 1 else 0
            ) // Use 1 for checked and 0 for unchecked
        }

        val result =
            db.update(TABLE_TODO_ITEM, values, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))

        // Log the result of the update
        if (result != 0) {
            Log.d("UpdateCheckbox", "Checkbox update successful for itemId: $itemId")
        } else {
            Log.e("UpdateCheckbox", "Checkbox update failed for itemId: $itemId")
        }

        db.close()
        return result != 0
    }


    // Method to delete a todo item
    fun deleteTodoItem(itemId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_TODO_ITEM, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))
        db.close()
        return result != 0
    }


}