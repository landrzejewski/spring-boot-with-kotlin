package pl.training.payments.adapters.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SpringDataJpaCardRepository : JpaRepository<CardEntity, String> {
    // CrudRepository<CardEntity, String>
    // Repository<CardEntity, String>

    fun findByNumber(number: String): CardEntity?

    @Query("select c from Card c where c.currencyCode = :currencyCode")
    fun findByCurrencyCode(currencyCode: String): List<CardEntity>

}