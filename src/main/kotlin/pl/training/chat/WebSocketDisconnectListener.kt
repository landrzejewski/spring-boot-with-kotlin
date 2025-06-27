package pl.training.chat

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import pl.training.chat.WebSocketUtils.getSocketId
import java.util.logging.Logger

@Component
class WebSocketDisconnectListener(
    private val systemMessageSender: SystemMessageSender,
    private val repository: InMemoryChatUserRepository
) {

    private val logger = Logger.getLogger(WebSocketDisconnectListener::class.java.name)

    @EventListener
    fun onConnect(event: SessionDisconnectEvent) {
        val socketId = getSocketId(event)
        repository.getUser(socketId)?.let {
            repository.removeUser(socketId)
            logger.info("Socket with id: ${it.name} is disconnected")
            systemMessageSender.sendToAll("User ${it.name} is disconnected")
        }
        systemMessageSender.updateUserList()
    }

}