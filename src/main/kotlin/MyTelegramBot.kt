import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import user.Task
import user.User
import java.io.File

class MyTelegramBot : TelegramLongPollingBot() {
    private val userFile = "users.json"
    private val taskFile = "tasks.json"
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    override fun getBotToken(): String {
            return "7876084312:AAEJex1BKKYjQcRhFFJVEHJgpDQ4jif_RXI" // Replace with your bot token
    }

    override fun getBotUsername(): String {
        return "@aimleetbot" // Replace with your bot username
    }
    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val chatId = update.message.chatId
            val userId = update.message.from.id.toString()
            val userFirstName = update.message.from.firstName
            val username = update.message.from.userName ?: "Unknown"

            // Register user
            registerUser(userId, userFirstName, username)

            // Create Inline Keyboard Buttons
            val profileButton = InlineKeyboardButton().apply {
                text = "👤 Profile"
                callbackData = "profile"
            }

            val balanceButton = InlineKeyboardButton().apply {
                text = "💰 Balance"
                callbackData = "balance"
            }

            val leetcodeButton = InlineKeyboardButton().apply {
                text = "📚 Leetcode Answers"
                callbackData = "leetcode"
            }

            val tasksButton = InlineKeyboardButton().apply {
                text = "📝 Tasks"
                callbackData = "tasks"
            }

            // Create a row with buttons
            val row1 = mutableListOf(profileButton, balanceButton)
            val row2 = mutableListOf(leetcodeButton, tasksButton)

            // Create the keyboard markup
            val keyboard = InlineKeyboardMarkup().apply {
                keyboard = mutableListOf(row1, row2) // Add rows to the keyboard
            }

            // Send a message with the buttons
            val message = SendMessage().apply {
                this.chatId = chatId.toString()
                this.text = "Hi ${userFirstName},\nSelect an option:"
                this.replyMarkup = keyboard
            }

            execute(message) // Send the message
        } else if (update.hasCallbackQuery()) {
            val callbackQuery = update.callbackQuery
            val chatId = callbackQuery.message.chatId

            // Handle button callbacks
            when (callbackQuery.data) {
                "profile" -> {
                    val profileMessage = SendMessage().apply {
                        this.chatId = chatId.toString()
                        this.text = "Here is your profile information.\n" +
                                "Username: ${callbackQuery.from.userName}\n" +
                                "Premium Status: ${getUserPrem(callbackQuery.from.id.toString())}"
                    }
                    execute(profileMessage)
                }
                "balance" -> {
                    val balanceMessage = SendMessage().apply {
                        this.chatId = chatId.toString()
                        this.text = "Your current balance is: ${getUserBalance(callbackQuery.from.id.toString())} coins"
                    }
                    execute(balanceMessage)
                }
                "leetcode" -> {
                    val leetcodeMessage = SendMessage().apply {
                        this.chatId = chatId.toString()
                        this.text = "Here are your LeetCode answers: [link]."
                    }
                    execute(leetcodeMessage)
                }
                "tasks" -> {
                    val tasksMessage = SendMessage().apply {
                        this.chatId = chatId.toString()
                        this.text = getTasks()
                    }
                    execute(tasksMessage)
                }
            }
        }
    }

    private fun registerUser(userId: String, firstName: String, username: String) {
        val users = readUsers()

        if (users.none { it.id == userId }) {
            val newUser = User(userId, firstName, username, prem = false) // Default to non-premium
            users.add(newUser)
            writeUsers(users)
            println("User registered: $newUser")
        }
    }

    private fun readUsers(): MutableList<User> {
        return if (File(userFile).exists()) {
            objectMapper.readValue(File(userFile), objectMapper.typeFactory.constructCollectionType(MutableList::class.java, User::class.java))
        } else {
            mutableListOf()
        }
    }

    private fun writeUsers(users: List<User>) {
        objectMapper.writeValue(File(userFile), users)
    }

    private fun getUserBalance(userId: String): Int {
        val users = readUsers()
        return users.find { it.id == userId }?.balance ?: 0
    }

    private fun getUserPrem(userId: String): String {
        val users = readUsers()
        return if (users.find { it.id == userId }?.prem == true) {
            "Premium User"
        } else {
            "Regular User"
        }
    }

    private fun getTasks(): String {
        val tasks = readTasks()
        return if (tasks.isNotEmpty()) {
            tasks.joinToString("\n") { it.description }
        } else {
            "No tasks available."
        }
    }

    private fun readTasks(): List<Task> {
        return if (File(taskFile).exists()) {
            objectMapper.readValue(File(taskFile), objectMapper.typeFactory.constructCollectionType(MutableList::class.java, Task::class.java))
        } else {
            mutableListOf()
        }
    }
}
fun main() {
    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    try {
        botsApi.registerBot(MyTelegramBot()) // Register your bot
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}
