package pl.training.payments.adapters.input.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.training.payments.adapters.input.rest.OperationTypeDto.INFLOW
import pl.training.payments.adapters.input.rest.OperationTypeDto.PAYMENT
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
        @RequestBody cardOperationRequestDto: CardOperationRequestDto
    ): ResponseEntity<Unit> {
        val cardNumber = mapper.toDomain(number)
        val amount = mapper.toDomain(cardOperationRequestDto)
        when (cardOperationRequestDto.operationType) {
            INFLOW -> cardOperations.inflow(cardNumber, amount)
            PAYMENT -> cardOperations.payment(cardNumber, amount)
        }
        return ResponseEntity.noContent().build()
    }

}