package com.android.todoapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: TodoDatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        dbHelper = TodoDatabaseHelper(this)

        val todoInput = findViewById<EditText>(R.id.todo_input)
        val addButton = findViewById<ImageButton>(R.id.add_button)

        addButton.setOnClickListener {
            val taskText = todoInput.text.toString()
            if (taskText.isNotEmpty()) {
                // Perform action with the task, like saving to a list or database
                Toast.makeText(this, "Task added: $taskText", Toast.LENGTH_SHORT).show()
                todoInput.text.clear()  // Clear the input after adding
            } else {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}