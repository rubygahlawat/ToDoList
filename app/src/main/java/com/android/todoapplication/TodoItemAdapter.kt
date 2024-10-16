package com.android.todoapplication

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodoItemAdapter(
    private val context: Context, // Context for starting activity
    private val todoList: List<Map<String, String>>, // List of tasks represented as maps
    private val db: TodoDatabaseHelper, // Pass the database helper to the adapter
    private val onTaskDeleted: () -> Unit, // Callback to notify deletion
) : RecyclerView.Adapter<TodoItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for each item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the TodoItem at the current position
        val todoItem = todoList[position] // Get the task map for the current position

        // Set the task name and due date
        holder.textViewTask.text = todoItem["taskName"] // Access task name from the map
        holder.textViewDueDate.text =
            todoItem["dueDate"] ?: "No Due Date"

        val isCompleted = todoItem["isCompleted"] == "Completed" // Check the task completion status
        holder.checkboxTask.isChecked = isCompleted // Set the checkbox state

        val dueDate = LocalDate.parse(todoItem["dueDate"], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val currentDate = LocalDate.now()

        // Highlight logic based on due date
        if (dueDate.isBefore(currentDate)) {
            // Change the text color to red for overdue items
            holder.textViewTask.setBackgroundColor(Color.RED)
        } else if (dueDate.isEqual(currentDate)) {
            // Change the text color to yellow for items due today
            holder.textViewTask.setBackgroundColor(Color.YELLOW)
        } else {
            // Change the text color to black for items not due yet
            holder.textViewTask.setTextColor(Color.BLACK)
        }

        // Set an OnCheckedChangeListener on the CheckBox
        holder.checkboxTask.setOnCheckedChangeListener { _, isChecked ->
            // Optional logging for debugging
            Log.d("checkboxTask", "Position: $position, isChecked: $isChecked")
            val result = db.updateCheckbox(todoItem["taskId"]?.toIntOrNull() ?: -1, isChecked)
            if (result) {
                Toast.makeText(
                    context,
                    if (isChecked) "Task is completed" else "Task not completed",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "Task not completed", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Edit Button Click
        holder.buttonEditTask.setOnClickListener {
            val intent = Intent(context, AddTodoActivity::class.java)
            // Pass task details to AddTodoActivity
            intent.putExtra(
                "TASK_ID",
                todoItem["taskId"]?.toIntOrNull()
            ) // Pass taskId from the map
            intent.putExtra("TASK_NAME", todoItem["taskName"]) // Pass taskName from the map
            intent.putExtra("TASK_DUE_DATE", todoItem["dueDate"]) // Pass dueDate from the map
            intent.putExtra("IS_COMPLETED", todoItem["isCompleted"]) // Pass dueDate from the map
            context.startActivity(intent)
        }

        // Handle Delete Button Click
        holder.buttonDeleteTask.setOnClickListener {
            deleteTask(todoItem["taskId"]?.toIntOrNull() ?: -1) // Safely get taskId
        }

        // Optional logging for debugging
        Log.d("TodoItemAdapter", "Position: $position, Task: $todoItem")
    }

    override fun getItemCount(): Int {
        return todoList.size // Return the size of the todoList
    }

    // ViewHolder class to hold the views for each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkboxTask: CheckBox =
            itemView.findViewById(R.id.checkboxTask) // Changed from TextView to CheckBox
        val textViewDueDate: TextView = itemView.findViewById(R.id.textViewDueDate)
        val textViewTask: TextView = itemView.findViewById(R.id.textViewTask)
        val buttonEditTask: ImageButton = itemView.findViewById(R.id.buttonEditTask)
        val buttonDeleteTask: ImageButton = itemView.findViewById(R.id.buttonDeleteTask)
    }

    // Function to delete a task
    private fun deleteTask(taskId: Int) {
        if (taskId == -1) {
            Toast.makeText(context, "Invalid task ID", Toast.LENGTH_SHORT).show()
            return
        }

        val result = db.deleteTodoItem(taskId) // Use the passed database helper instance
        if (result) {
            Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show()
            // Notify the activity to refresh the layout
            onTaskDeleted()
        } else {
            Toast.makeText(context, "Error deleting task", Toast.LENGTH_SHORT).show()
        }
    }
}
