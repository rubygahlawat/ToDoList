package com.android.todoapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoListAdapter(
    private var todoLists: List<String>,
    private val context: Context
) : RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder>() {

    // ViewHolder class for the RecyclerView
    class TodoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listNameTextView: TextView = itemView.findViewById(R.id.list_name_text_view)

        // The bind function now accepts a click listener
        fun bind(todoItem: String, onItemClick: (Int) -> Unit) {
            listNameTextView.text = todoItem // Use listNameTextView to set text
            itemView.setOnClickListener {
                // Trigger the click listener with the current position
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListViewHolder {
        // Inflate the layout for each item in the RecyclerView
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo_list, parent, false)
        return TodoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoListViewHolder, position: Int) {
        val todoItem = todoLists[position] // Get the item at the current position
        holder.bind(todoItem) { index ->
            // Start TodoItemListActivity with the selected todo item and index
            val intent = Intent(context, TodoItemListActivity::class.java).apply {
                putExtra("TODO_ITEM", todoItem) // Pass the todo item title
                putExtra("INDEX", index) // Pass the index
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        // Return the number of items in the list
        return todoLists.size
    }

    // Method to update the list
    fun updateTodoList(newTodoLists: List<String>) {
        todoLists = newTodoLists
        notifyDataSetChanged() // Notify the adapter of the data change
    }
}