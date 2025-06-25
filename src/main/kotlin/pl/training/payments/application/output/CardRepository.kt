package pl.training.payments.application.output

import pl.training.commons.model.PageSpec
import pl.training.commons.model.ResultPage
import pl.training.payments.domain.Card
import pl.training.payments.domain.CardNumber

interface CardRepository {

    fun getByNumber(cardNumber: CardNumber): Card?

    fun getAll(pageSpec: PageSpec): ResultPage<Card>

    fun save(card: Card)

}
