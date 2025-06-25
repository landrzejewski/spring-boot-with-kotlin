package pl.training.payments.adapters.input.rest

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import pl.training.commons.model.validation.Base
import pl.training.commons.model.validation.Extended
import pl.training.commons.model.validation.MonetaryAmount
import java.math.BigDecimal

class CardOperationRequestDto(
    @field:Min(100, groups = [Base::class]) @field:MonetaryAmount(maxValue = 1.000, groups = [Extended::class]) val amount: BigDecimal,
    @field:Pattern(regexp = "^[a-zA-Z]+$", groups = [Base::class]) val currencyCode: String,
    val operationType: OperationTypeDto
)
