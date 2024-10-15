package com.android.todoapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AddTodoActivity : AppCompatActivity() {

    private lateinit var TodoDatabaseHelper: TodoDatabaseHelper
    private var taskID: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        val dueDateEditText = findViewById<EditText>(R.id.dueDate)
        val editTextTaskName = findViewById<EditText>(R.id.editTextName)
        val submitButton = findViewById<Button>(R.id.btnSubmit)

        // Initialize TaskDatabaseHelper
        TodoDatabaseHelper = TodoDatabaseHelper(this)

        // Retrieve task details and index from Intent
        val listId = intent.getIntExtra("LIST_ID",-1)
        val taskName = intent.getStringExtra("TASK_NAME")
        val dueDate = intent.getStringExtra("TASK_DUE_DATE")
        taskID = intent.getIntExtra("TASK_ID", -1)

        // Prefill the fields if editing an existing task
        taskName?.let { editTextTaskName.setText(it) }
        dueDate?.let { dueDateEditText.setText(it) }

        val calendar = Calendar.getInstance()
        dueDateEditText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    dueDateEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year, month, day
            )
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        submitButton.setOnClickListener {
            val updatedTaskName = editTextTaskName.text.toString().trim()
            val updatedDueDate = dueDateEditText.text.toString().trim()

            if (updatedTaskName.isEmpty()) {
                Toast.makeText(this, "Please enter task name", Toast.LENGTH_SHORT).show()
            } else {
                // Check if editing an existing task
                if (taskID != -1) {
                    // Update the task in the SQLite database
                    val result = TodoDatabaseHelper.updateTodoItem(taskID, updatedTaskName, updatedDueDate)
                    if (result) {
                        Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Add a new task if no index is passed
                    val result = TodoDatabaseHelper.insertTodoItem(updatedTaskName, updatedDueDate, listId)
                    if (result != -1L) {
                        Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
                        editTextTaskName.text.clear()
                        dueDateEditText.text.clear()
                        finish()
                    } else {
                        Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

