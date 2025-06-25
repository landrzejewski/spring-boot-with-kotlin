package pl.training.payments.adapters.input.rest

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import pl.training.commons.model.validation.Base
import pl.training.commons.model.validation.Extended
import pl.training.commons.web.ExceptionDto
import pl.training.payments.adapters.input.rest.OperationTypeDto.INFLOW
import pl.training.payments.adapters.input.rest.OperationTypeDto.PAYMENT
import pl.training.payments.application.CardNotFoundException
import pl.training.payments.application.CardOperationsService

@RequestMapping("api/cards")
@RestController
class CardOperationsRestController(
    private val cardOperations: CardOperationsService,
    private val mapper: CardRestMapper
) {

    @PostMapping("{number:\\d{16,19}}/operations")
    fun addOperation(
        @PathVariable number: String,
        /*@Valid*/ @Validated(Extended ::class) @RequestBody cardOperationRequestDto: CardOperationRequestDto
    ): ResponseEntity<Unit> {
        val cardNumber = mapper.toDomain(number)
        val amount = mapper.toDomain(cardOperationRequestDto)
        when (cardOperationRequestDto.operationType) {
            INFLOW -> cardOperations.inflow(cardNumber, amount)
            PAYMENT -> cardOperations.payment(cardNumber, amount)
        }
        return ResponseEntity.noContent().build()
    }

    /*@ExceptionHandler(CardNotFoundException::class)
    fun onCardNotFound(exception: CardNotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionDto("Card not found"))*/

}