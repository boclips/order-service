package com.boclips.terry.domain.service

import com.boclips.terry.domain.exceptions.IllegalCurrencyException
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

class FxRateServiceTest {
    private val fxRateService = FxRateService()

    @Test
    fun `calculates the fx rate between two currencies to 5dp`() {
        val fxRates = mapOf(
            Currency.getInstance("USD") to BigDecimal(0.5),
            Currency.getInstance("AUD") to BigDecimal(0.75)
        )

        val resolvedFxRate = fxRateService.resolve(fxRates, Currency.getInstance("USD"), Currency.getInstance("AUD"))
        assertThat(resolvedFxRate).isEqualTo(BigDecimal.valueOf(1.50000).setScale(5, RoundingMode.HALF_UP))
    }

    @Test
    fun `throws illegal currency exception when converting unknown currency`() {
        assertThrows<IllegalCurrencyException> {
            fxRateService.resolve(emptyMap(), Currency.getInstance("USD"), Currency.getInstance("AUD"))
        }
    }
}
