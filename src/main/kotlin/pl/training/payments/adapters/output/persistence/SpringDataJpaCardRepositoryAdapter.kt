package pl.training.payments.adapters.output.persistence

import org.springframework.transaction.annotation.Transactional
import pl.training.commons.annotations.Adapter
import pl.training.commons.model.PageSpec
import pl.training.commons.model.ResultPage
import pl.training.payments.application.output.CardRepository
import pl.training.payments.domain.Card
import pl.training.payments.domain.CardNumber

@Transactional
@Adapter
class SpringDataJpaCardRepositoryAdapter(
    private val repository: SpringDataJpaCardRepository,
    private val mapper: SpringDataJpaCardRepositoryMapper
) : CardRepository {

    override fun getByNumber(cardNumber: CardNumber): Card? {
        val number = mapper.toEntity(cardNumber)
        return repository.findByNumber(number)?.let { mapper.toDomain(it) }
    }

    override fun getAll(pageSpec: PageSpec): ResultPage<Card> {
        val pageRequest = mapper.toEntity(pageSpec)
        var cardEntitiesPage = repository.findAll(pageRequest)
        return mapper.toDomain(cardEntitiesPage)
    }

    override fun save(card: Card) {
        val cardEntity = mapper.toEntity(card)
        repository.save(cardEntity)
    }

}