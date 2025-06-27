package pl.training.chat

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import pl.training.chat.WebSocketUtils.getNativeHeader
import pl.training.chat.WebSocketUtils.getSocketId
import java.util.logging.Logger

@Component
class WebSocketConnectListener(
    private val systemMessageSender: SystemMessageSender,
    private val repository: InMemoryChatUserRepository
) {

    private val logger = Logger.getLogger(WebSocketConnectListener::class.java.name)

    @EventListener
    fun onConnect(event: SessionConnectEvent) {
        val socketId = getSocketId(event)
        val user = createUser(event)
        repository.addUser(socketId, user)
        logger.info("Socket with id: ${user.name} is connected")
        systemMessageSender.sendToAll("User ${user.name} is connected")
        systemMessageSender.updateUserList()
    }

    private fun createUser(event: SessionConnectEvent): ChatUser {
        val username = getNativeHeader(event, USERNAME_HEADER)
        val clientId = getNativeHeader(event, CLIENT_ID_HEADER)
        val privateClientId = getNativeHeader(event, PRIVATE_CLIENT_ID_HEADER)
        return ChatUser(clientId, privateClientId, username)
    }

    private companion object {
        const val USERNAME_HEADER = "username"
        const val CLIENT_ID_HEADER = "clientId"
        const val PRIVATE_CLIENT_ID_HEADER = "privateClientId"
    }

}