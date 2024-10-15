package com.android.todoapplication

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoListAdapter(
    private var todoLists: List<Map<String, Any>>,
    private val context: Context
) : RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder>() {

    // ViewHolder class for the RecyclerView
    inner class TodoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewListName: TextView = itemView.findViewById(R.id.list_name_text_view)
        val textViewCounters: TextView = itemView.findViewById(R.id.task_counters)

        fun bind(listName: String, onItemClick: (Int) -> Unit) {
            textViewListName.text = listName
            itemView.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo_list, parent, false)
        return TodoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoListViewHolder, position: Int) {
        val list = todoLists[position]
        val listName = list["listName"] as String
        val totalItems = list["totalItems"] as Int
        val completedItems = list["completedItems"] as Int

        holder.textViewListName.text = listName
        holder.textViewCounters.text = "Completed: $completedItems / Total: $totalItems"
        holder.bind(listName) { index ->
            val intent = Intent(context, TodoItemListActivity::class.java).apply {
                putExtra("TODO_ITEM", listName)
                putExtra("INDEX", index)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return todoLists.size
    }

    fun updateTodoList(newTodoLists: List<Map<String, Any>>) {
        todoLists = newTodoLists
        notifyDataSetChanged()
    }
}
