package pl.training.security.jwt.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.collections.toList
import kotlin.jvm.java

@Service
class JwtService(@Value("\${jwt.secret}") secret: String) {

    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .build()

    fun createToken(username: String, roles: Set<String>) = JWT.create()
        .withIssuer(ISSUER)
        .withClaim(USER_CLAIM, username)
        .withClaim(ROLES_CLAIM, roles.toList())
        .withExpiresAt(Instant.now().plusSeconds(EXPIRATION_PERIOD))
        .sign(algorithm)

    fun verify(token: String) =
        try {
            val jwt = verifier.verify(token)
            val username = jwt.getClaim(USER_CLAIM).asString()
            val roles = jwt.getClaim(ROLES_CLAIM).asList(String::class.java)
            JwtPrincipal(username, HashSet(roles))
        } catch (_: JWTVerificationException) {
            null
        }

    companion object {

        private const val ISSUER = "https://localhost:8000"
        private const val USER_CLAIM = "user"
        private const val ROLES_CLAIM = "roles"
        private const val EXPIRATION_PERIOD = 3600L

    }

}
