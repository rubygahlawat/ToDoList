package com.android.todoapplication

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
            intent.putExtra(
                "LIST_ID",
                itemIndex + 1
            ) // Pass the index to AddTodoActivity for new tasks
            startActivityForResult(intent, ADD_TASK_REQUEST_CODE) // Start for result
        }
    }

    private fun loadTasks() {
        // Log fetching tasks
        Log.d(TAG, "Fetching tasks for list index: $itemIndex")

        // Get tasks from the database (as List<Triple<String, String?, Int, Boolean>>)
        val tasks = db.getTodoItemsForList(itemIndex + 1)

        // Log the fetched tasks
        Log.d(TAG, "Fetched tasks: $tasks")

        // Create a list of maps for task details
        val tasksList: List<Map<String, String>> = tasks.map { task ->
            val taskId = task.taskId.toString() // Get the taskId (Int converted to String)
            val taskName = task.taskName  // Get the task name (String)
            val dueDate = task.dueDate ?: "No Due Date" // Get the due date, provide default if null
            val isCompleted =
                if (task.isCompleted) "Completed" else "Not Completed" // Get completion status
            val listId = itemIndex + 1
            // Create a map for task details
            mapOf(
                "listId" to listId.toString(),
                "taskId" to taskId,
                "taskName" to taskName,
                "dueDate" to dueDate, // dueDate is guaranteed to be a String
                "isCompleted" to isCompleted // Represent task completion as a string
            )
        }

        // Log the tasks list as key-value pairs
        Log.d(TAG, "Tasks as key-value pairs: $tasksList")

        // Create and set the adapter with the list of task maps
        todoAdapter = TodoItemAdapter(this, tasksList, db) {
            loadTasks() // Reload tasks on task deletion
        }
        recyclerViewTasks.adapter = todoAdapter
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(
            TAG,
            "onActivityResult called with requestCode: $requestCode, resultCode: $resultCode"
        )
        if (requestCode == ADD_TASK_REQUEST_CODE && resultCode == RESULT_OK) {
            Log.d(TAG, "Reloading tasks after adding or editing")
            loadTasks()
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks() // Reload tasks every time the activity is resumed
    }

    companion object {
        const val ADD_TASK_REQUEST_CODE = 1
    }
}

