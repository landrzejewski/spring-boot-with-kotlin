package pl.training.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RequestMapping("api/users/me")
@RestController
class UserRestController {

    @GetMapping
    fun getUser(authentication: Authentication, principal: Principal): UserDetails {
        val auth = SecurityContextHolder.getContext().authentication
        return auth.principal as UserEntity
    }

}