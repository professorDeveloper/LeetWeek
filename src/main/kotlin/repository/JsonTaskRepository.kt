package repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import domain.Task
import java.io.File
import java.util.StringJoiner
import java.util.UUID

class JsonTaskRepository(private val taskFile: String = "tasks.json") : TaskRepository {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    // Retrieves all tasks from the JSON file
    override fun getAllTasks(): List<Task> {
        return try {
            val file = File(taskFile)
            if (file.exists()) {
                objectMapper.readValue(file)
            } else {
                emptyList() // Return empty list if file does not exist
            }
        } catch (e: Exception) {
            println("Error reading tasks from file: ${e.message}")
            emptyList() // Return empty list on error
        }
    }

    // Retrieves a task by its ID
    override fun getTaskById(taskId: String): Task? {
        return getAllTasks().find { it.id == taskId } // Find task by ID
    }

    override fun addTask(task: Task) {
        val tasks = getAllTasks().toMutableList() // Get current tasks
        tasks.add(task) // Add the new task
        saveTasks(tasks) // Save updated tasks

    }

    // Adds a new task with user ID, description, and completion status
    override fun addTask(userId: String, taskDescription: String, isDone: Boolean, taskTitle: String): String {
        val newTask = Task(
            id = UUID.randomUUID().toString(), // Generate a unique ID for the task
            userId = userId,
            task = taskTitle,
            taskDescription = taskDescription,
            isDone = isDone,
        )

        val tasks = getAllTasks().toMutableList() // Get current tasks
        tasks.add(newTask) // Add the new task
        saveTasks(tasks) // Save updated tasks

        return newTask.id // Return the ID of the newly added task
    }

    // Updates an existing task
    override fun updateTask(task: Task) {
        val tasks = getAllTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == task.id } // Find index by ID
        if (index != -1) {
            tasks[index] = task // Update the task
            saveTasks(tasks) // Save updated tasks
        } else {
            println("Task not found: ${task.id}") // Task not found
        }
    }

    // Retrieves tasks by user ID
    override fun getTasksByUserId(userId: String): List<Task> {
        return getAllTasks().filter { it.userId == userId } // Filter tasks by user ID
    }

    // Saves the list of tasks back to the JSON file
    private fun saveTasks(tasks: List<Task>) {
        try {
            objectMapper.writeValue(File(taskFile), tasks) // Write tasks to file
        } catch (e: Exception) {
            println("Error writing tasks to file: ${e.message}")
        }
    }
}
