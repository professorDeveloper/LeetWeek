package domain

data class User(
    val id: String,
    val firstName: String,
    val username: String,
    var balance: Int = 0,
    var prem: Boolean = false,
    var tasks: MutableList<Task> = mutableListOf() // Initialize with an empty list
)
