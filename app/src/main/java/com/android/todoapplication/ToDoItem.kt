package com.android.todoapplication

data class TodoItem(
    val taskName: String,
    val dueDate: String?,
    val taskId: Int,
    val isCompleted: Boolean,
)

