package domain

import com.fasterxml.jackson.databind.BeanDescription
import java.util.StringJoiner

data class Task(
    val id: String,           // Unique identifier for the task
    val userId: String,
    val taskDescription: String,// ID of the user to whom the task belongs
    val task: String,        // Description of the task
    val isDone: Boolean =false     // Completion status of the task
)
