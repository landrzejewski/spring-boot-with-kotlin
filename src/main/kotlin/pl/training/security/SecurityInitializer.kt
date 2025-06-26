package pl.training.security

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class SecurityInitializer(
    private val userRepository: JpaUserRepository,
    private val passwordEncoder: PasswordEncoder,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        if (userRepository.findByLogin(LOGIN) == null) {
            val user = UserEntity(
                login = LOGIN,
                secret = passwordEncoder.encode(SECRET),
                verified = true,
                cin = "001",
                roles = ROLES
            )
            userRepository.save(user)
        }
    }

    companion object {

        private const val LOGIN = "admin"
        private const val SECRET = "admin"
        private const val ROLES = "ROLE_ADMIN,ROLE_MANAGER"

    }

}