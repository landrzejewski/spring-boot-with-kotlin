package pl.training.security.jwt

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import pl.training.security.jwt.util.JwtService
import kotlin.collections.map
import kotlin.jvm.java
import kotlin.let

// @Component
class JwtAuthenticationProvider(private val jwtService: JwtService) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication) =
        if (authentication is JwtAuthentication) {
            val token = authentication.token
            jwtService.verify(token)?.let {
                val roles = it.roles.map { role -> SimpleGrantedAuthority(role) }
                val auth = JwtAuthentication(token, it.username, roles)
                auth.isAuthenticated = true
                auth
            } ?: throw BadCredentialsException("Invalid token")
        } else {
            null
        }

    override fun supports(authentication: Class<*>) =
        JwtAuthentication::class.java.isAssignableFrom(authentication)

}