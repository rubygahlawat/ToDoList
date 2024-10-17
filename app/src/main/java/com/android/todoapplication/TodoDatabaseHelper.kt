package com.android.todoapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.appcompat.app.AlertDialog

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
    fun insertTodoList(context: Context,listName: String):Boolean {
        val db = this.writableDatabase

        // Check if the list name already exists
        val cursor = db.query(TABLE_TODO_LIST,arrayOf(COLUMN_LIST_NAME),"$COLUMN_LIST_NAME = ?",arrayOf(listName),null,null,null)

        // If the cursor has any rows, it means the list name exists
        if (cursor.count > 0) {
            // Show an alert dialog to inform the user
            showAlertDialog(context)
            // List name already exists, handle the situation (e.g., log a message or return)
            cursor.close() // Close the cursor
            db.close() // Close the database
            return false// Exit the function
        }

        // If it does not exist, proceed to insert the new list
        val values = ContentValues().apply {
            put(COLUMN_LIST_NAME, listName)
        }

        val result = db.insert(TABLE_TODO_LIST, null, values)
        cursor.close() // Close the cursor
        db.close() // Close the database
        return result != -1L
    }


    // Update a  todo list name
    fun updateListName(context: Context,listId: Int, listName: String): Boolean {
        val db = this.writableDatabase
        // Check if the list name already exists
        val cursor = db.query(TABLE_TODO_LIST,arrayOf(COLUMN_LIST_NAME),"$COLUMN_LIST_NAME = ?",arrayOf(listName),null,null,null)

        // If the cursor has any rows, it means the list name exists
        if (cursor.count > 0) {
            showAlertDialog(context)
            // List name already exists, handle the situation (e.g., log a message or return)
            cursor.close() // Close the cursor
            db.close() // Close the database
            return false// Exit the function
        }

        val values = ContentValues().apply {
            put(COLUMN_LIST_NAME, listName)
        }

        // Log the incoming parameters
        Log.d("UpdateTodoItem","Attempting to update listName for listId: $listId with new name: $listName" )

        // Perform the update operation
        val result =db.update(TABLE_TODO_LIST, values, "$COLUMN_LIST_ID = ?", arrayOf(listId.toString()))

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
                val nearestDueDate =
                    cursor.getString(cursor.getColumnIndexOrThrow("nearestDueDate"))
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
                val taskId =cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID)) // Return as Int
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
            put(
                COLUMN_ITEM_NAME,
                taskName
            ) // Assuming COLUMN_ITEM_NAME is the name of the column for task names
            put(
                COLUMN_ITEM_DUE_DATE,
                dueDate
            ) // Assuming COLUMN_DUE_DATE is the name of the column for due dates
        }

        val result =
            db.update(TABLE_TODO_ITEM, values, "$COLUMN_ITEM_ID = ?", arrayOf(itemId.toString()))

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
    fun getAvailableLists(excludeListId: Int): List<Map<String, Any>> {
        val availableLists = mutableListOf<Map<String, Any>>()
        val db = this.readableDatabase

        // Query to get all lists except the one with the passed listId
        val cursor = db.query(
            TABLE_TODO_LIST, // The table to query
            arrayOf(COLUMN_LIST_ID, COLUMN_LIST_NAME), // The columns to return
            "$COLUMN_LIST_ID != ?", // The WHERE clause to exclude the listId
            arrayOf(excludeListId.toString()), // The value for the WHERE clause
            null, // Group the rows
            null, // Filter by row groups
            null // The sort order
        )

        // Loop through the cursor and add each list to the availableLists
        while (cursor.moveToNext()) {
            val listId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIST_ID))
            val listName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_NAME))

            availableLists.add(mapOf(
                "listId" to listId,
                "listName" to listName
            ))
        }

        cursor.close() // Close the cursor to avoid memory leaks
        return availableLists // Return the list of available lists
    }


//    fun moveList(currentListId: Int, taskId: Int, targetListId: Int): Boolean {
//        val db = this.writableDatabase
//        db.beginTransaction()
//
//        Log.d("TodoApp", "Starting to move item with taskId=$taskId from listId=$currentListId to listId=$targetListId")
//
//        return try {
//            // Set the new list ID for the specific item
//            val contentValues = ContentValues().apply {
//                put(COLUMN_ITEM_LIST_ID, targetListId) // Set the new list ID
//            }
//
//            Log.d("TodoApp", "Updating item with taskId=$taskId in listId=$currentListId to new listId=$targetListId")
//
//            // Update the specific item (using taskId) to the new list
//            val rowsUpdated = db.update(
//                TABLE_TODO_ITEM, // The table to update
//                contentValues, // The new values to set
//                "$COLUMN_ITEM_LIST_ID = ? AND $COLUMN_ITEM_ID = ?", // The WHERE clause
//                arrayOf(currentListId.toString(), taskId.toString()) // The values for the WHERE clause
//            )
//
//            Log.d("TodoApp", "Rows updated: $rowsUpdated")
//
//            // If any rows were updated, the transaction is marked as successful
//            if (rowsUpdated > 0) {
//                db.setTransactionSuccessful()
//                Log.d("TodoApp", "Transaction successful for moving taskId=$taskId")
//                true // Return true if the update was successful
//            } else {
//                Log.d("TodoApp", "No rows were updated for taskId=$taskId")
//                false // Return false if no rows were updated
//            }
//        } catch (e: Exception) {
//            Log.e("TodoApp", "Error occurred while moving taskId=$taskId", e)
//            false // Return false in case of an error
//        } finally {
//            db.endTransaction()
//            Log.d("TodoApp", "Transaction ended for moving taskId=$taskId")
//        }
//    }
fun moveList(currentListId: Int, taskId: Int, targetListId: Int): Boolean {
    val db = this.writableDatabase
    db.beginTransaction()

    Log.d("TodoApp", "Moving item with taskId=$taskId from listId=$currentListId to listId=$targetListId")

    return try {
        // Step 1: Update the listId for the specific item to move it
        val contentValues = ContentValues().apply {
            put(COLUMN_ITEM_LIST_ID, targetListId) // Set the new list ID
        }

        Log.d("TodoApp", "Updating item with taskId=$taskId to new listId=$targetListId")

        // Update the specific item (using taskId) to the new list
        val rowsUpdated = db.update(
            TABLE_TODO_ITEM, // The table to update
            contentValues, // The new values to set
            "$COLUMN_ITEM_ID = ?", // The WHERE clause
            arrayOf(taskId.toString()) // Use only taskId for the WHERE clause
        )

        Log.d("TodoApp", "Rows updated: $rowsUpdated")

        // Step 2: Check if any rows were updated and commit the transaction
        if (rowsUpdated > 0) {
            db.setTransactionSuccessful()
            Log.d("TodoApp", "Transaction successful for moving taskId=$taskId")
            true // Return true if the update was successful
        } else {
            Log.d("TodoApp", "No rows were updated for taskId=$taskId")
            false // Return false if no rows were updated
        }
    } catch (e: Exception) {
        Log.e("TodoApp", "Error occurred while moving taskId=$taskId", e)
        false // Return false in case of an error
    } finally {
        db.endTransaction()
        Log.d("TodoApp", "Transaction ended for taskId=$taskId")
    }
}



    private fun showAlertDialog(context: Context) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Duplicate List Name")
            .setMessage("A list with this name already exists. Please choose a different name.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()

        alertDialog.show()
    }
}