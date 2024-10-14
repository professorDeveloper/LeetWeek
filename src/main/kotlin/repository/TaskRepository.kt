// repository/TaskRepository.kt
package repository

import domain.Task
import javax.print.DocFlavor.STRING

interface TaskRepository {
    fun getAllTasks(): List<Task>
    fun getTaskById(taskId: String): Task?
    fun addTask(task: Task,)
    fun updateTask(task: Task)
    fun getTasksByUserId(userId: String): List<Task>
    fun addTask(userId: String, taskDescription: String, isDone: Boolean,taskTitle:String): String // Returns the task ID

}
