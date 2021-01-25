package com.boclips.orders.common

import com.boclips.orders.presentation.carts.TrimServiceRequest
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.ReportAsSingleViolation
import kotlin.reflect.KClass

@Constraint(validatedBy = [TrimmingValidator::class])
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.RUNTIME)
@ReportAsSingleViolation
annotation class ValidateTrimming(
    val message: String = ": trim values cannot be null",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)

class TrimmingValidator : ConstraintValidator<ValidateTrimming, Specifiable<TrimServiceRequest>?> {
    override fun isValid(request: Specifiable<TrimServiceRequest>?, context: ConstraintValidatorContext?): Boolean {
        request ?: return true

        return when (request) {
            is ExplicitlyNull -> true
            is Specified -> request.value.let {
                it.from != null && it.to != null &&
                    checkFormat(it.from, context) && checkFormat(it.to, context) &&
                    checkValuesTogether(it.from, it.to, context)
            }
        }
    }

    private fun checkFormat(value: String, context: ConstraintValidatorContext?): Boolean {
        val isValid = value.matches("""(^\d+:[0-5]\d$)""".toRegex())
        if (!isValid) {
            buildConstraintMessage(": $value is not a valid time format", context)
        }
        return isValid
    }

    private fun checkValuesTogether(from: String, to: String, context: ConstraintValidatorContext?): Boolean {
        val isValid = (from.replace(":", "").toInt() < to.replace(":", "").toInt())
        if (!isValid) {
            buildConstraintMessage(": 'from' value must be before 'to' value", context)
        }
        return isValid
    }

    private fun buildConstraintMessage(message: String, context: ConstraintValidatorContext?) {
        context?.disableDefaultConstraintViolation()
        context
            ?.buildConstraintViolationWithTemplate(message)
            ?.addConstraintViolation()
    }
}
