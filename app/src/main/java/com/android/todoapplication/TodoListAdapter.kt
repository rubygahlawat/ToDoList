package com.android.todoapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoListAdapter(private var todoLists: List<String>) :
    RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder>() {

    // ViewHolder class for the RecyclerView
    class TodoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listNameTextView: TextView = itemView.findViewById(R.id.list_name_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListViewHolder {
        // Inflate the layout for each item in the RecyclerView
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo_list, parent, false)
        return TodoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoListViewHolder, position: Int) {
        // Bind the data to each view holder
        holder.listNameTextView.text = todoLists[position]
    }

    override fun getItemCount(): Int {
        // Return the number of items in the list
        return todoLists.size
    }
    // Method to update the list
    fun updateTodoList(newTodoLists: List<String>) {
        todoLists = newTodoLists
        notifyDataSetChanged()
    }
}
