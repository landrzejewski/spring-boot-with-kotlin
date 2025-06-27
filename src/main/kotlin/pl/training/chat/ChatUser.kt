package pl.training.chat

data class ChatUser(
    val id: String,
    val privateId: String,
    val name: String,
    val hidden: Boolean = false,
    val busy: Boolean = false
)