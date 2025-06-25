package pl.training.commons.model.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Constraint(validatedBy = [UniqueTitleValidator::class])
@Target(FIELD)
annotation class UniqueTitle(

    val message: String = "should be unique",

    val groups: Array<KClass<*>> = [],

    val payload: Array<KClass<out Payload>> = []

)