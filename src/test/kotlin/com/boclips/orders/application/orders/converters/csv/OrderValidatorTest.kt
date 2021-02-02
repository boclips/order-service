package com.boclips.orders.application.orders.converters.csv

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OrderValidatorTest {

    @Test
    fun `when valid value `() {
        val errors = mutableListOf<OrderConversionError>()
        val setter = mock<(prop: String) -> Unit>()

        OrderValidator(null, errors)
            .setNotNullOrError("value", setter, "irrelevant")

        assertThat(errors).isEmpty()
        verify(setter).invoke("value")
    }

    @Test
    fun `when null value adds default error message`() {
        val errors = mutableListOf<OrderConversionError>()
        val setter = mock<(prop: String) -> Unit>()

        OrderValidator(null, errors)
            .setNotNullOrError(null, setter, "error message")

        assertThat(errors.map { it.message }).containsExactly("error message")
        verifyZeroInteractions(setter)
    }

    @Test
    fun `when setter throws adds default error message`() {
        val errors = mutableListOf<OrderConversionError>()

        OrderValidator(null, errors)
            .setNotNullOrError("irrelevant", { throw RuntimeException() }, "error message")

        assertThat(errors.map { it.message }).containsExactly("error message")
    }

    @Test
    fun `when setter throws specified error then adds accompanying error message`() {
        val errors = mutableListOf<OrderConversionError>()

        OrderValidator(null, errors).setNotNullOrError(
            "irrelevant",
            { throw RuntimeException("too bad") },
            "default error message",
            java.lang.RuntimeException::class to "special error"
        )

        assertThat(errors.map { it.message }).containsExactly("special error: too bad")
    }
}
