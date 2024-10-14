# Telegram Bot with Firebase Integration

This project implements a Telegram bot in Kotlin that connects to Firebase to manage tasks, balance (coins), and user data. The bot retrieves tasks, shows the user's coin balance, and stores new user data when a user interacts with the bot for the first time.

## Project Structure

```bash
src
├── bot
│   └── MyTelegramBot.kt          # Handles Telegram bot interactions
├── domain
│   ├── Task.kt                   # Data model representing a Task
│   └── User.kt                   # Data model representing a User
├── repository
│   ├── JsonTaskRepository.kt      # Handles task data storage and retrieval
│   ├── JsonUserRepository.kt      # Handles user data storage and retrieval
│   ├── TaskRepository.kt          # Abstract interface for Task data
│   └── UserRepository.kt          # Abstract interface for User data
├── Main.kt                        # Entry point of the application
└── (other helper packages)        # Additional packages and utilities
