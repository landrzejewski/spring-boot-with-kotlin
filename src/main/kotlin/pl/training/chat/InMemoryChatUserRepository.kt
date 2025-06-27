package pl.training.chat

import org.springframework.stereotype.Repository
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Stream
import kotlin.concurrent.read
import kotlin.concurrent.write

@Repository
class InMemoryChatUserRepository {

    private val users = mutableMapOf<String, ChatUser>()
    private val lock = ReentrantReadWriteLock()

    fun addUser(socketId: String, user: ChatUser) = lock.write { users[socketId] = user }

    fun getUser(socketId: String): ChatUser? = lock.read { users[socketId] }

    fun removeUser(socketId: String) = lock.write { users.remove(socketId) }

    fun getAllUsers(): List<ChatUser> = lock.read { users.values.toList() }

    fun findByClientIds(clientIds: Set<String>): Stream<ChatUser> = lock.read {
        users.values.stream().filter { it.id in clientIds }
    }

    fun updateStatus(socketId: String, isHidden: Boolean) = lock.write {
        getUser(socketId)?.let { users[socketId] = it.copy(hidden = isHidden) }
    }

}