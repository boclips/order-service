package com.boclips.orders.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PriceTest {
    @Test
    fun `amount is always 2 decimal places`() {
        val price = Price(amount = BigDecimal.valueOf(100.987), currency = null)
        assertThat(price.amount).isEqualTo(BigDecimal.valueOf(100.99))
    }

    @Test
    fun `amount rounds up when above half`() {
        val price = Price(amount = BigDecimal.valueOf(1.015), currency = null)
        assertThat(price.amount).isEqualTo(BigDecimal.valueOf(1.02))
    }

    @Test
    fun `amount rounds down when below half`() {
        val price = Price(amount = BigDecimal.valueOf(1.014), currency = null)
        assertThat(price.amount).isEqualTo(BigDecimal.valueOf(1.01))
    }
}
