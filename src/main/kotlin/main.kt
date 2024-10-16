import bot.MyTelegramBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import repository.JsonTaskRepository
import repository.JsonUserRepository

fun main() {
    val userRepository = JsonUserRepository("users.json")
    val taskRepository = JsonTaskRepository("tasks.json")


    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    try {
        botsApi.registerBot(MyTelegramBot(userRepository, taskRepository))
        println("Bot is running...")
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}

//class Solution {
//    fun addStrings(num1: String, num2: String): String {
//        if ()
//    }
//}

// Removed the initializeTasksIfNeeded function entirely
