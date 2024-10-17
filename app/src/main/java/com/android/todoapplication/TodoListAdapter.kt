package com.android.todoapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodoListAdapter(
    private var todoLists: List<Map<String, Any>>,
    private val context: Context,
    private val db: TodoDatabaseHelper,
) : RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder>() {

    // ViewHolder class for the RecyclerView
    inner class TodoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editButton: ImageButton =
            itemView.findViewById(R.id.edit_list_name_button) // Correct type for ImageButton
        val textViewListName: TextView = itemView.findViewById(R.id.list_name_text_view)
        val textViewCounters: TextView = itemView.findViewById(R.id.task_counters)
        val dueDateTextView: TextView = itemView.findViewById(R.id.dueDateTextView)

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TodoListViewHolder, position: Int) {
        val list = todoLists[position]
        val listName = list["listName"] as String
        val totalItems = list["totalItems"] as Int
        val completedItems = list["completedItems"] as Int

        holder.textViewListName.text = listName
        holder.textViewCounters.text = "Completed: $completedItems / Total: $totalItems"

        // Assuming "dueDate" is a String in "yyyy-MM-dd" format
        val dueDateString = list["dueDate"] as String
        holder.dueDateTextView.text = "Due Date of nearest item: $dueDateString"
        // Set up click listener to navigate to the TodoItemListActivity
        holder.bind(listName) { index ->
            val intent = Intent(context, TodoItemListActivity::class.java).apply {
                putExtra("TODO_ITEM", listName)
                putExtra("INDEX", index)
            }
            context.startActivity(intent)
        }

        // Set up the edit button click listener
        holder.editButton.setOnClickListener {
            // Create a dialog to edit the list name
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Edit List Name")

            // Set up the input field with the current name
            val input = EditText(holder.itemView.context).apply {
                setText(listName)  // Use the current list name from the `list` map
            }

            // Create a TextView for the error message
            val errorMessage = TextView(holder.itemView.context).apply {
                visibility = View.GONE // Initially hidden
                setTextColor(Color.RED) // Set text color to red
            }

            // Create a layout to hold the EditText and error message
            val layout = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.VERTICAL
                addView(input)
                addView(errorMessage) // Add error message below input
            }

            // Set the layout in the dialog
            builder.setView(layout)

            // Set up dialog buttons
            builder.setPositiveButton("OK", null) // Set positive button to null to handle manually
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            // Show the dialog
            val dialog = builder.create()
            dialog.show()

            // Set up the positive button click listener
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newName = input.text.toString().trim()
                // Log the incoming parameters
                Log.d("UpdateTodoItem", "Attempting to update listName: $newName")

                if (newName.isNotEmpty()) {
                    // Update the list name in the UI
                    holder.textViewListName.text = newName
                    // Try updating the list name in the database
                    try {
                        val result = db.updateListName(context,
                            list["listId"] as Int,
                            newName
                        )  // Assuming `id` is stored as an Int
                        if (result) {  // Assuming `updateListName` returns the number of affected rows
                            Toast.makeText(
                                context,
                                "List name updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Update the name in the data set
                            todoLists = todoLists.toMutableList().apply {
                                this[position] =
                                    list.toMutableMap().apply { this["listName"] = newName }
                            }
                            notifyItemChanged(position)  // Notify the adapter of the change
                        } else {
                            Toast.makeText(
                                context,
                                "Error: List name not updated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        // Handle the error (e.g., show a toast)
                        Toast.makeText(
                            holder.itemView.context,
                            "Error updating list name",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dialog.dismiss()
                } else {
                    // Inform the user that the name cannot be empty
                    errorMessage.text = "List name cannot be null or empty"
                    errorMessage.visibility = View.VISIBLE // Show the error message
                    input.requestFocus() // Optionally, focus back on the input field
                }
            }
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
