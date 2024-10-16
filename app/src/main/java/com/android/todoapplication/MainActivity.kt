package com.android.todoapplication

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: TodoDatabaseHelper
    private lateinit var todoListRecyclerView: RecyclerView
    private lateinit var adapter: TodoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // This method is used for edge-to-edge display on supported devices
        setContentView(R.layout.activity_main)

        // Set up the current date TextView
        val currentDateTextView: TextView = findViewById(R.id.current_date_text_view)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        currentDateTextView.text = "Today Date: $currentDate"

        // Initialize the database helper
        dbHelper = TodoDatabaseHelper(this)

        // Set up RecyclerView
        todoListRecyclerView = findViewById(R.id.todo_list_recycler_view)
        todoListRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch lists with task counters
        val todoListsWithCounts = dbHelper.getTodoListsWithCounts()
        Log.d("todoListsWithCounts", "Item at position $todoListsWithCounts")
        adapter = TodoListAdapter(todoListsWithCounts, this, dbHelper)
        todoListRecyclerView.adapter = adapter

        // Set up input for new tasks
        val todoInput = findViewById<EditText>(R.id.todo_input)
        val addButton = findViewById<ImageButton>(R.id.add_button)

        // Add button click listener
        addButton.setOnClickListener {
            val taskText = todoInput.text
            if (taskText.isNotEmpty()) {
                // Insert the task into the SQLite database
                dbHelper.insertTodoList(taskText.toString())
                Toast.makeText(this, "Task added: $taskText", Toast.LENGTH_SHORT).show()

                // Fetch updated lists with task counters
                val updatedTodoLists = dbHelper.getTodoListsWithCounts()
                adapter.updateTodoList(updatedTodoLists)  // Update the adapter with new data

                todoInput.text.clear()  // Clear the input after adding
            } else {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
