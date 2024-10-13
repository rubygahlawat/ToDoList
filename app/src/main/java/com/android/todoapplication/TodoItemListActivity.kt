package com.android.todoapplication

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TodoItemListActivity : AppCompatActivity() {

    private var itemIndex: Int = -1 // Declare an index variable to store the item index
    private lateinit var db: TodoDatabaseHelper
    private lateinit var recyclerViewTasks: RecyclerView
    private lateinit var todoAdapter: TodoItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.todo_item_list) // Ensure this layout contains the necessary views

        // Get the TODO item and its index from the Intent
        val todoItem = intent.getStringExtra("TODO_ITEM") ?: "No Item"
        itemIndex = intent.getIntExtra("INDEX", -1) // Default to -1 if not found

        // Initialize the database helper
        db = TodoDatabaseHelper(this)

        // Set up the RecyclerView
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks)
        recyclerViewTasks.layoutManager = LinearLayoutManager(this)

        loadTasks() // Load tasks from the database

        // Find the button by its ID
        val btnAddTask = findViewById<Button>(R.id.btnAddTask)

        // Set an OnClickListener on the button
        btnAddTask.setOnClickListener {
            // Create an Intent to start the AddTodoActivity
            val intent = Intent(this, AddTodoActivity::class.java)
            intent.putExtra("ITEM_INDEX", itemIndex + 1) // Pass the index to AddTodoActivity
            startActivity(intent) // Start the new activity
        }

        // Optional: Log the retrieved item and index for debugging purposes
        Log.d(ContentValues.TAG, "Todo Item: $todoItem, Index: $itemIndex")
    }

    private fun loadTasks() {
        // Add logging before fetching tasks
        Log.d(TAG, "Fetching tasks for list index: $itemIndex")

        // Get tasks from the database
        val tasks = db.getTodoItemsForList(itemIndex+1)

        // Log the fetched tasks
        Log.d(TAG, "Fetched tasks: $tasks")

        // Create and set the adapter
        todoAdapter = TodoItemAdapter(tasks)
        recyclerViewTasks.adapter = todoAdapter
    }
}