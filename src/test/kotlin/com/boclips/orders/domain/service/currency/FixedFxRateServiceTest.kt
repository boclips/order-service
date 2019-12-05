package com.boclips.orders.domain.service.currency

import com.boclips.orders.domain.exceptions.IllegalCurrencyException
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

class FixedFxRateServiceTest {

    @Test
    fun `calculates the fx rate between two currencies to 5dp`() {
        val fxRateService = FixedFxRateService(
            mapOf(
                Currency.getInstance("USD") to BigDecimal(0.5),
                Currency.getInstance("AUD") to BigDecimal(0.75)
            )
        )

        val resolvedFxRate = fxRateService.resolve(Currency.getInstance("USD"), Currency.getInstance("AUD"))
        assertThat(resolvedFxRate).isEqualTo(BigDecimal.valueOf(1.50000).setScale(5, RoundingMode.HALF_UP))
    }

    @Test
    fun `throws illegal currency exception when converting unknown currency`() {
        val fxRateService = FixedFxRateService(emptyMap())
        assertThrows<IllegalCurrencyException> {
            fxRateService.resolve(Currency.getInstance("USD"), Currency.getInstance("AUD"))
        }
    }
}
