package pl.training.chat

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.lang.Exception
import java.time.Instant
import java.util.logging.Logger

class WebSocketHandshakeInterceptor : HandshakeInterceptor {

    private val logger = Logger.getLogger(WebSocketHandshakeInterceptor::class.java.name)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        attributes["connectionTimestamp"] = Instant.now()
        if (request is ServletServerHttpRequest) {
            logger.info("Handshake request: ${request.uri}")
        }
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        // no-op
    }

}