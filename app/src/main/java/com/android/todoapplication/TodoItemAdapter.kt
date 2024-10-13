package com.android.todoapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoItemAdapter(private val todoList: List<Pair<String, String?>>) : RecyclerView.Adapter<TodoItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for each item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Destructure the pair into taskName and dueDate
        val (taskName, dueDate) = todoList[position]

        // Set the task name to the TextView
        holder.textViewTask.text = taskName // Corrected line

        // Set due date or a default message
        holder.textViewDueDate.text = dueDate ?: "No Due Date"

        // Optional logging for debugging
        Log.d("TodoItemAdapter", "Position: $position, Task: $taskName, Due Date: $dueDate")
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    // ViewHolder class to hold the views for each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTask: TextView = itemView.findViewById(R.id.textViewTask) // Ensure this ID matches your XML
        val textViewDueDate: TextView = itemView.findViewById(R.id.textViewDueDate) // Ensure this ID matches your XML
    }
}