package pl.training.chat

import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class SystemMessageSender(
    @Value("\${user-list-topic}") private val userListTopic: String,
    @Value("\${main-topic}") private val mainTopic: String,
    @Value("\${time-topic}") private val timeTopic: String,
    private val messagingTemplate: SimpMessagingTemplate,
    private val repository: InMemoryChatUserRepository,
    @Value("\${update-contacts-delay-in-mills}") private val updateDelay: Long,
    private val taskScheduler: TaskScheduler
) {

    fun sendToAll(text: String) {
        val chatMessage = ChatMessage(SYSTEM_SENDER, text, timestamp = Instant.now())
        messagingTemplate.convertAndSend(mainTopic, chatMessage)
    }

    fun updateUserList() {
        val chatUsers = repository.getAllUsers()
            .map { it.copy(privateId = "") }
            .filter { !it.hidden }
        messagingTemplate.convertAndSend(userListTopic, chatUsers)
    }

    fun updateUserTime() {
        messagingTemplate.convertAndSend(timeTopic, Instant.now())
    }

    companion object {

        private const val SYSTEM_SENDER = "System"

    }

}