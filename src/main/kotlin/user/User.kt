package user

data class User(val id: String, val firstName: String, val username: String, var balance: Int = 0, var prem: Boolean = false)
data class Task(val description: String)
