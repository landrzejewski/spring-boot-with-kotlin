package pl.training.payments.adapters.input.rest

import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pl.training.commons.web.RestExceptionResponseBuilder
import pl.training.payments.application.CardNotFoundException
import pl.training.payments.domain.InsufficientFundsException
import java.util.*

@Order(HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = ["pl.training.payments.adapters.input.rest"])
class CardRestExceptionHandler(
    private val exceptionResponseBuilder: RestExceptionResponseBuilder
) {

    /*@ExceptionHandler(CardNotFoundException::class)
    fun onCardNotFound(exception: CardNotFoundException) =
        ResponseEntity.status(NOT_FOUND).body(ExceptionDto("Card not found"))*/

    @ExceptionHandler(CardNotFoundException::class)
    fun onCardNotFound(exception: CardNotFoundException, locale: Locale) =
        exceptionResponseBuilder.build(exception, NOT_FOUND, locale)

    @ExceptionHandler(InsufficientFundsException::class)
    fun onInsufficientFunds(exception: InsufficientFundsException, locale: Locale) =
        exceptionResponseBuilder.build(exception, BAD_REQUEST, locale)

}