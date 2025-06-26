package pl.training.security

import org.springframework.data.repository.CrudRepository

interface JpaUserRepository : CrudRepository<UserEntity, Long> {

    fun findByLogin(login: String): UserEntity?

}