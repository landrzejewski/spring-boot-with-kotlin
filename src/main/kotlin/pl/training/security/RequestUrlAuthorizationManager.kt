package pl.training.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.util.function.Supplier
import kotlin.collections.any
import kotlin.collections.map
import kotlin.collections.toSet
import kotlin.to

class RequestUrlAuthorizationManager : AuthorizationManager<RequestAuthorizationContext> {

    override fun check(
        authentication: Supplier<Authentication>,
        context: RequestAuthorizationContext
    ): AuthorizationDecision {
        val request = context.request
        val hasAccess = getRoles(authentication)
            .any { role -> check(role, request) }
        return AuthorizationDecision(hasAccess)
    }

    private fun getRoles(authentication: Supplier<Authentication>) =
        authentication.get()
            .authorities
            .map { it.authority }
            .toSet()

    private fun check(role: String, request: HttpServletRequest) = mappings
        .getOrDefault(role, emptySet())
        .any { matcher -> matcher.matches(request) }

    companion object {

        private val mappings = mapOf(
            "ROLE_ADMIN" to setOf(
                AntPathRequestMatcher("/**")
            ),
            "ROLE_USER" to setOf(
                AntPathRequestMatcher("/api/**")
            ),
        )

    }

}