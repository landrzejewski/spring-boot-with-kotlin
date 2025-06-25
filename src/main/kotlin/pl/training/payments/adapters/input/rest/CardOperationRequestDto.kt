package pl.training.payments.adapters.input.rest

import java.math.BigDecimal

class CardOperationRequestDto(
    val amount: BigDecimal,
    val currencyCode: String,
    val operationType: OperationTypeDto
)
