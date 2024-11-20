// bot/MyTelegramBot.kt
package bot

import domain.Task
import domain.User
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.EntityType.URL
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import repository.TaskRepository
import repository.UserRepository
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.Serializable
import java.lang.reflect.Method
import java.net.URL
import java.util.UUID

class MyTelegramBot(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository
) : TelegramLongPollingBot() {
    private val waitingForInput = mutableMapOf<String, String>()
    private val MAX_LINES_PER_MESSAGE = 10 // Har bir xabar uchun maksimal qator soni

    private fun downloadedFile(fileId: String): File? {
        // Create the GetFile object and set the fileId
        var getFile=GetFile();
        getFile.fileId=fileId
        val execute = execute(getFile)
        // Execute the GetFile request to fetch the file

        // Construct the download URL using the valid filePath
        val fileUrl = "https://api.telegram.org/file/bot$botToken/${execute.filePath}"

        return try {
            val url = URL(fileUrl)
            println("fileUrlasdasd$fileUrl")
            val inputStream = url.openStream()
            val localFile = File("downloaded_file") // Name this as needed
            inputStream.use { input ->
                localFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            localFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getBotToken(): String {
        return "7876084312:AAEJex1BKKYjQcRhFFJVEHJgpDQ4jif_RXI" // Replace with your actual bot token
    }

    override fun getBotUsername(): String {
        return "@aimleetbot" // Replace with your bot's username
    }
    private fun sendResultFileToUser(chatId: String, file: File) {
        val sendDocument = SendDocument()
        sendDocument.chatId = chatId

        // Create the InputFile object from the local result file
        val inputFile = InputFile(file, file.name)

        // Attach the file to the message
        sendDocument.document = inputFile
        sendDocument.caption = "Here are the search results:"

        // Send the file
        try {
            execute(sendDocument) // This sends the file
        } catch (e: Exception) {
            e.printStackTrace()
            sendMessage(chatId, "Failed to send the results file.")
        } finally {
            file.delete() // Clean up the file after sending
        }
    }

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()){
            val chatId = update.message.chatId.toString()
            val message=update.message
            if (waitingForInput[chatId.toString()] == "searchTerm") {
                val searchTerm = message.text
                println("asdasdasd")
                println(searchTerm)
                waitingForInput.remove(chatId) // Clear the state for this user
                sendMessage(chatId,"Waiting...")
                // Now you can use the searchTerm variable for your search functionality
                 searchInFile(chatId = chatId,searchTerm)

                // Send the results back to the user
                return // Exit early since we've handled the input
            }

        }
        if (update != null && update.hasMessage() && update.message.hasDocument()) {
            val document = update.message.document
            val fileId = document.fileId
            val chatId = update.message.chatId.toString()
            // Check if we're waiting for input from this user
            println("Fileleeelele:$fileId")

            // Get file path and download the file
            val file = downloadedFile(fileId)
            if (file != null) {
                // Process the file (e.g., read its contents)
                sendformat(file, chatId = chatId.toString())
            }
        }

        when {
            update.hasMessage() && update.message.hasText() -> handleMessage(update)
            update.hasCallbackQuery() -> handleCallbackQuery(update)
        }
    }
    private fun sendformat(file: File, chatId: String) {
        // Process the input file and generate result.txt
        file.useLines { lines ->
            File("result.txt").printWriter().use { out ->
                lines.forEach { line ->
                    var processedLine = line

                    // Loop through the range of numbers and remove the prefixes
                    for (i in 1..123456) {
                        processedLine = processedLine.removePrefix("$i.txt:")
                        processedLine = processedLine.removePrefix("Base$i.txt:")
                        processedLine = processedLine.removePrefix("Database$i.txt:")
                    }

                    // Write the processed line to the output file only if it's not empty
                    if (processedLine.isNotBlank()) {
                        out.println(processedLine.trim())
                    }
                }
            }
        }

        // Send the result.txt file to the user
        sendResultFileToUser(chatId, "result.txt")
    }

    private fun sendResultFileToUser(chatId: String, filePath: String) {
        val sendDocument = SendDocument()
        sendDocument.chatId = chatId

        // Create the InputFile object from the local result.txt file
        val resultFile = File(filePath)
        val inputFile = InputFile(resultFile, resultFile.name)

        // Attach the file to the message
        sendDocument.document = inputFile

        // Add a caption if needed
        sendDocument.caption = "Here is the processed result file."

        // Send the file
        try {
            execute(sendDocument) // This sends the file
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private val waitingForSearchTerm = mutableMapOf<String, Boolean>()

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
                listOf(createButton("📚 Format", "format")),
                listOf(createButton("📝 Tasks", "tasks")),
                listOf(createButton("\uD83D\uDD0D grep", "grep"))
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

    private fun sendMessage(chatId: String, text: String) {
        val sendMessage = SendMessage(chatId, text)
        execute(sendMessage) // Send the message
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
            "grep" -> {
                waitingForInput[chatId] = "searchTerm" // Indicate that we're waiting for a search term
                sendMessage(chatId, "Please enter the text you want to search for:")
            }
            callbackQuery.data.contains("task_button") ?: "" -> handleCompleteTask(
                chatId,
                userId,
                callbackData = callbackQuery.data
            )


            else -> executeMessage(SendMessage(chatId, "Unknown command."))
        }
    }
    private fun searchInFile(chatId: String,searchTerm: String )  {
        val results = mutableListOf<String>()
        val filepath="D:\\DATABASE\\1.txt";
        var count =0;

        File(filepath).useLines { lines ->
            lines.forEach { line ->
                count++
                if (line.contains(searchTerm, ignoreCase = true)) {
                    println(line+"Resulttt")
                    results.add(line) // Add matching lines to the results
                }
            }
        }

        // Write results to a new text file
        val resultFile = File("results.txt")
        resultFile.printWriter().use { out ->
            if (results.isNotEmpty()) {
                results.forEach { out.println(it) }
            } else {
                out.println("No results found for '$searchTerm'.")
            }
        }

        // Send the result file to the user
        sendResultFileToUser(chatId, resultFile)
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
            this.text = "Your current balance is: ${getUserBalance(userId)}$    "
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