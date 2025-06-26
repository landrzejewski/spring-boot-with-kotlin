package pl.training.security.jwt

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class JwtAuthentication(
    val token: String,
    val user: Any? = null,
    val roles: Collection<GrantedAuthority> = emptyList()
) : AbstractAuthenticationToken(roles) {

    override fun getCredentials() = token

    override fun getPrincipal() = user

}