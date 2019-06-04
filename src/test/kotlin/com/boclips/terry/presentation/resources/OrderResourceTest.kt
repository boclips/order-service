package com.boclips.terry.presentation.resources

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal

class OrderResourceTest {
    @Test
    fun `convert price to price resource`() {
        val price = BigDecimal.valueOf(1.12)
        assertThat(PriceResource.fromBigDecimal(price)).isEqualTo(PriceResource(value = price, displayValue = "£1.12"))
    }

    @Test
    fun `convert price with lots of decimal places to price resource`() {
        val price = BigDecimal.valueOf(1.1212321321)
        assertThat(PriceResource.fromBigDecimal(price)).isEqualTo(PriceResource(value = price, displayValue = "£1.12"))
    }

    @Test
    fun `convert price with no decimal places to two decimal places`() {
        val price = BigDecimal.valueOf(100)
        assertThat(PriceResource.fromBigDecimal(price)).isEqualTo(
            PriceResource(
                value = price,
                displayValue = "£100.00"
            )
        )
    }
}