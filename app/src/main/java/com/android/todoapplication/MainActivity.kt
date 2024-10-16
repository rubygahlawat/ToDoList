package com.android.todoapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: TodoDatabaseHelper
    private lateinit var todoListRecyclerView: RecyclerView
    private lateinit var adapter: TodoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbHelper = TodoDatabaseHelper(this)

        todoListRecyclerView = findViewById(R.id.todo_list_recycler_view)

        todoListRecyclerView.layoutManager = LinearLayoutManager(this)


        // Fetch data from SQLite and display it
        val todoLists = dbHelper.getAllTodoLists()
        adapter = TodoListAdapter(todoLists,this)

        todoListRecyclerView.adapter = adapter

        val todoInput = findViewById<EditText>(R.id.todo_input)
        val addButton = findViewById<ImageButton>(R.id.add_button)

        addButton.setOnClickListener {
            val taskText = todoInput.text.toString()
            if (taskText.isNotEmpty()) {
                // Insert the task into the SQLite database
                dbHelper.insertTodoList(taskText)
                Toast.makeText(this, "Task added: $taskText", Toast.LENGTH_SHORT).show()

                // Fetch updated data and notify the adapter
                val updatedTodoLists = dbHelper.getAllTodoLists()
                adapter.updateTodoList(updatedTodoLists)  // Call a custom method in the adapter

                todoInput.text.clear()  // Clear the input after adding
            } else {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
