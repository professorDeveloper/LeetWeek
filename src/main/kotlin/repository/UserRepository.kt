package repository

import domain.User

interface UserRepository {
    fun getAllUsers(): List<User>                      // Retrieve all users
    fun getUserById(userId: String): User?            // Retrieve a user by ID
    fun addUser(user: User)                            // Add a new user
    fun updateUser(user: User) // Update an existing user
    fun userExists(userId: String):Boolean
}
