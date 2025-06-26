package pl.training.security

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.core.CredentialsContainer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity(name = "User")
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue
    val id: Long? = null,
    val login: String,
    var secret: String,
    val verified: Boolean,
    val cin: String,
    val roles: String,
) : UserDetails, CredentialsContainer {

    override fun getAuthorities() = roles.split(ROLE_SEPARATOR)
        .map { it.trim() }
        .map { SimpleGrantedAuthority(it) }
        .toSet()

    override fun getPassword() = secret

    override fun getUsername() = login

    override fun isEnabled() = verified

    override fun eraseCredentials() {
        secret = ""
    }

    companion object {

        private const val ROLE_SEPARATOR = ","

    }

}