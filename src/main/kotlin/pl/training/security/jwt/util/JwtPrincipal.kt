package pl.training.security.jwt.util

class JwtPrincipal(
    val username: String,
    val roles: Set<String>
)
