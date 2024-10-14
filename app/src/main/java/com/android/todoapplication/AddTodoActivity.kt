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
    // Initialize your database helper
    private lateinit var TodoDatabaseHelper: TodoDatabaseHelper
    private var itemIndex: Int = -1 // Variable to hold the index

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        // Retrieve the index passed from the previous activity
        itemIndex = intent.getIntExtra("ITEM_INDEX", -1) // Default value is -1 if not found

        val dueDateEditText = findViewById<EditText>(R.id.dueDate)
        val editTextTaskName = findViewById<EditText>(R.id.editTextName)
        val submitButton = findViewById<Button>(R.id.btnSubmit)

        // Initialize TaskDatabaseHelper
        TodoDatabaseHelper = TodoDatabaseHelper(this)

        // Create a Calendar instance to get the current date
        val calendar = Calendar.getInstance()

        // Set an OnClickListener for the EditText
        dueDateEditText.setOnClickListener {
            // Get the current year, month, and day
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Create a DatePickerDialog to show the calendar
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format the selected date and display it in the EditText
                    dueDateEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year,
                month,
                day
            )
            // Disable previous dates
            datePickerDialog.datePicker.minDate = calendar.timeInMillis

            // Show the DatePickerDialog
            datePickerDialog.show()
        }

        // Set OnClickListener for the Submit button
        submitButton.setOnClickListener {
            val taskName = editTextTaskName.text.toString().trim()
            val dueDate = dueDateEditText.text.toString().trim()
            val listId = itemIndex // Use the passed index
// Add a log to print the value of listId
            Log.d("AddTodoActivity", "List ID: $listId") // Log the listId value

            if (taskName.isEmpty()) {
                // Show a message if task name or due date is empty
                Toast.makeText(this, "Please enter task name", Toast.LENGTH_SHORT).show()
            } else {
                // Insert task into SQLite database
                val result = TodoDatabaseHelper.insertTodoItem(taskName, dueDate, listId)
                if (result != -1L) {
                    Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
                    // Clear the input fields after adding the task
                    editTextTaskName.text.clear()
                    dueDateEditText.text.clear()
                    val resultIntent = Intent()
                    resultIntent.putExtra("TASK_ADDED", true)
                    setResult(RESULT_OK, resultIntent)
                    // Redirect to the previous layout
                    finish()
                } else {
                    Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
