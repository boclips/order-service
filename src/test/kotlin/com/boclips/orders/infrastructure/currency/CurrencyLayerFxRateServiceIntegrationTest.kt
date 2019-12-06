package com.boclips.orders.infrastructure.currency

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

class CurrencyLayerFxRateServiceIntegrationTest {

    val service = CurrencyLayerFxRateService(System.getenv("CURRENCY_LAYER_ACCESS_KEY"))

    @Test
    fun convert() {
        val rate = service.getRate(
            from = Currency.getInstance("GBP"),
            to = Currency.getInstance("USD"),
            on = LocalDate.parse("2019-12-01")
        )

        assertThat(rate).isCloseTo(BigDecimal("1.3"), Offset.offset(BigDecimal("0.01")))
    }

    @Test
    fun `convert throws a meaningful exception when request is not successful`() {
        assertThatCode {
            service.getRate(
                from = Currency.getInstance("GBP"),
                to = Currency.getInstance("USD"),
                on = LocalDate.parse("2099-12-01")
            )
        }.hasMessageContaining("invalid date")
    }

    @Test
    fun `returns 1 when source and target currencies are the same`() {
        val rate = service.getRate(
            from = Currency.getInstance("PLN"),
            to = Currency.getInstance("PLN"),
            on = LocalDate.now().plusDays(10)
        )

        assertThat(rate).isEqualTo("1")
    }
}