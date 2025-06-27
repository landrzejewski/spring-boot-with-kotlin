package pl.training.chat

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class TimeProvider(private val systemMessageSender: SystemMessageSender) {

    @Scheduled(fixedRate = 1_000)
    fun updateClientTime() {
        systemMessageSender.updateUserTime()
    }

}