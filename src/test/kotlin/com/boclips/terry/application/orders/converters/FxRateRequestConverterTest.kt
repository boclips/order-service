package com.boclips.terry.application.orders.converters

import com.boclips.terry.presentation.orders.PoundFxRateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Currency

class FxRateRequestConverterTest {
    @Test
    fun `converts to currencies against GBP`() {
        val request = PoundFxRateRequest(
            eur = BigDecimal.valueOf(1.12),
            usd = BigDecimal.valueOf(0.76),
            sgd = BigDecimal.valueOf(0.88),
            aud = BigDecimal.valueOf(1.433)
        )

        val fxRatesAgainstGBP = FxRateRequestConverter.convert(request)

        assertThat(fxRatesAgainstGBP).isEqualTo(
            mapOf(
                Currency.getInstance("EUR") to BigDecimal.valueOf(1.12),
                Currency.getInstance("USD") to BigDecimal.valueOf(0.76),
                Currency.getInstance("SGD") to BigDecimal.valueOf(0.88),
                Currency.getInstance("AUD") to BigDecimal.valueOf(1.433),
                Currency.getInstance("GBP") to BigDecimal.ONE
            )
        )
    }
}
