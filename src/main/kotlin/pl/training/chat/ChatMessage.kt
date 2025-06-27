package pl.training.chat

import java.time.Instant

data class ChatMessage(
    val sender: String,
    val text: String,
    val recipients: Set<String> = emptySet(),
    val timestamp: Instant? = null,
) {

    fun isBroadcast() = recipients.isEmpty()

}