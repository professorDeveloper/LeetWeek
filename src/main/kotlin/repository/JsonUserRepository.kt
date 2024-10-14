package repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import domain.User
import java.io.File

class JsonUserRepository(private val userFile: String = "users.json") : UserRepository {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    override fun getAllUsers(): List<User> {
        return try {
            if (File(userFile).exists()) {
                objectMapper.readValue(File(userFile))
            } else {
                emptyList() // Return empty list if file does not exist
            }
        } catch (e: Exception) {
            println("Error reading users from file: ${e.message}")
            emptyList() // Return empty list on error
        }
    }

    override fun getUserById(userId: String): User? {
        return getAllUsers().find { it.id == userId } // Change 'id' to 'id'
    }

    override fun addUser(user: User) {
        val users = getAllUsers().toMutableList() // Get current users
        users.add(user) // Add new user
        saveUsers(users) // Save updated users
    }

    override fun updateUser(user: User) {
        val users = getAllUsers().toMutableList()
        val index = users.indexOfFirst { it.id == user.id } // Change 'id' to 'id'
        if (index != -1) {
            users[index] = user // Update the user
            saveUsers(users) // Save updated users
        } else {
            println("User not found: ${user.id}") // User not found
        }
    }

    override fun userExists(userId: String): Boolean {
        return getAllUsers().any { it.id == userId }
    }


    private fun saveUsers(users: List<User>) {
        try {
            objectMapper.writeValue(File(userFile), users) // Write users to file
        } catch (e: Exception) {
            println("Error writing users to file: ${e.message}")
        }
    }
}
