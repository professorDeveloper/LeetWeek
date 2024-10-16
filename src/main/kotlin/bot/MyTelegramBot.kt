// bot/MyTelegramBot.kt
package bot

import domain.Task
import domain.User
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import repository.TaskRepository
import repository.UserRepository
import java.util.UUID

class MyTelegramBot(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) : TelegramLongPollingBot() {

    override fun getBotToken(): String {
        return "7876084312:AAEJex1BKKYjQcRhFFJVEHJgpDQ4jif_RXI" // Replace with your actual bot token
    }

    override fun getBotUsername(): String {
        return "@aimleetbot" // Replace with your bot's username
    }

    override fun onUpdateReceived(update: Update) {
        when {
            update.hasMessage() && update.message.hasText() -> handleMessage(update)
            update.hasCallbackQuery() -> handleCallbackQuery(update)
        }
    }

    private fun handleMessage(update: Update) {
        val chatId = update.message.chatId.toString()
        val userId = update.message.from.id.toString()
        val userFirstName = update.message.from.firstName
        val username = update.message.from.userName ?: "Unknown"

        // Register the user in the database
        if (!userRepository.userExists(userId)) { // Foydalanuvchi allaqachon mavjudligini tekshirish
            registerUser(userId, userFirstName, username)
        }

        // Create inline keyboard buttons
        val keyboard = createMainMenuKeyboard()

        // Send message with the inline keyboard
        val message = SendMessage().apply {
            this.chatId = chatId
            this.text = "Hi $userFirstName,\nSelect an option:"
            this.replyMarkup = keyboard
        }

        executeMessage(message)
    }

    private fun createMainMenuKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = listOf(
                listOf(createButton("👤 Profile", "profile")),
                listOf(createButton("💰 Balance", "balance")),
                listOf(createButton("📚 Leetcode", "leetcode")),
                listOf(createButton("📝 Tasks", "tasks"))
            )
        }
    }

    private fun createButton(text: String, callbackData: String): InlineKeyboardButton {
        return InlineKeyboardButton().apply {
            this.text = text
            this.callbackData = callbackData
        }
    }

    private fun registerUser(userId: String, firstName: String, username: String) {
        // Register the user logic
        userRepository.addUser(User(userId, firstName, username, 0, false)) // Foydalanuvchini qo'shish
        // Add default task for new users
        taskRepository.addTask(
            Task(
                userId = userId,
                id = UUID.randomUUID().toString(),
                taskDescription = "Follow me on GitHub",
                task = "Task 1",
                isDone = false
            )
        ) // Yangilash
        println("User registered: $firstName ($username) with a default task added.")
    }


    private fun handleCallbackQuery(update: Update) {
        val callbackQuery = update.callbackQuery
        val chatId = callbackQuery.message.chatId.toString()
        val userId = callbackQuery.from.id.toString()

        when (callbackQuery.data) {
            "profile" -> sendProfileInfo(chatId, userId)
            "balance" -> sendBalanceInfo(chatId, userId)
            "leetcode" -> sendLeetcodeAnswers(chatId)
            "tasks" -> sendTasks(chatId, userId)//
            callbackQuery.data.contains("task_button") ?: "" -> handleCompleteTask(
                chatId,
                userId,
                callbackData = callbackQuery.data
            )


            else -> executeMessage(SendMessage(chatId, "Unknown command."))
        }
    }

    private fun handleCompleteTask(chatId: String, userId: String, callbackData: String) {
        // Extract the task ID from the callback data
        val taskId = callbackData.removePrefix("complete_task_")

        // Mark the task as completed in the repository
        val task = taskRepository.getTaskById(taskId)
        if (task != null && task.userId == userId) {
            if (!task.isDone) {
//                taskRepository.updateTaskStatus(taskId, true)
                val responseMessage = "✅ Task '${task.task}' has been marked as completed!"
                executeMessage(SendMessage(chatId, responseMessage))
            } else {
                executeMessage(SendMessage(chatId, "⚠️ This task is already completed."))
            }
        } else {
            executeMessage(SendMessage(chatId, "❌ Task not found or you don't have permission to modify it."))
        }

        // Optionally, refresh the tasks list
        sendTasks(chatId, userId)
    }


    private fun sendProfileInfo(chatId: String, userId: String) {
        val profileMessage = SendMessage().apply {
            this.chatId = chatId
            this.text = "Here is your profile information:\n" +
                    "User ID: $userId\n" +
                    "Username: ${userRepository.getUserById(userId)?.username ?: "Unknown"}"
        }
        executeMessage(profileMessage)
    }

    private fun sendBalanceInfo(chatId: String, userId: String) {
        val balanceMessage = SendMessage().apply {
            this.chatId = chatId
            this.text = "Your current balance is: ${getUserBalance(userId)} coins"
        }
        executeMessage(balanceMessage)
    }

    private fun sendLeetcodeAnswers(chatId: String) {
        val leetcodeMessage = SendMessage().apply {
            this.chatId = chatId
            this.text = "Here are your LeetCode answers: [link]."
        }
        executeMessage(leetcodeMessage)
    }

    private fun sendTasks(chatId: String, userId: String) {
        val tasks = taskRepository.getTasksByUserId(userId) // Fetch tasks from repository
        val keyboard = InlineKeyboardMarkup()

        // Creating buttons for each task
        val buttons = tasks.map { task ->
            createButton("Complete: ${task.task}", "task_button${task.id}")
        }.chunked(1) // Create rows of buttons (1 task per row for clarity)

        keyboard.keyboard = buttons


        val tasksMessage = SendMessage().apply {
            this.chatId = chatId
            this.text = "You have the following tasks:"
            this.replyMarkup = keyboard
        }
        executeMessage(tasksMessage)
    }

    private fun executeMessage(message: SendMessage) {
        try {
            execute(message)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    private fun getUserBalance(userId: String): Int {
        // Fetch the user's balance
        return userRepository.getUserById(userId)?.balance ?: 0
    }
}