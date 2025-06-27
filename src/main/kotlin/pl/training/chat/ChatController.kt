package pl.training.chat

import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import java.time.Instant

@Controller
class ChatController(
    @Value("\${main-topic}") private val mainTopic: String,
    @Value("\${private-topic-prefix}") private val privateTopicPrefix: String,
    private val messagingTemplate: SimpMessageSendingOperations,
    private val repository: InMemoryChatUserRepository,
    private val systemMessageSender: SystemMessageSender
) {

    @MessageMapping("/chat")
    fun onMessage(
        chatMessage: ChatMessage,
        @Header("simpSessionId") socketId: String/*, accessor: SimpMessageHeaderAccessor*/
    ) {
        // val attributes = accessor.sessionAttributes
        val message = chatMessage.copy(timestamp = Instant.now())
        if (message.isBroadcast()) {
            messagingTemplate.convertAndSend(mainTopic, message)
        } else {
            repository.getUser(socketId)?.let { sendPrivateMessage(it, message) }
            repository.findByClientIds(message.recipients).forEach { sendPrivateMessage(it, message) }
        }
    }

    private fun sendPrivateMessage(user: ChatUser, message: ChatMessage) =
        messagingTemplate.convertAndSend(privateTopicPrefix + user.privateId, message)

    @MessageMapping("/statuses")
    fun onStatusUpdate(chatStatus: ChatStatus, @Header("simpSessionId") socketId: String) {
        repository.updateStatus(socketId, chatStatus.hidden, chatStatus.busy)
        systemMessageSender.updateUserList()
    }

    @MessageMapping("/readiness")
    fun onReady() {
        systemMessageSender.updateUserList()
    }

    /*@MessageMapping("/chat")
    @SendTo("/main")
    fun onMessage(chatMessage: ChatMessage) = chatMessage.copy(timestamp = Instant.now())*/

}