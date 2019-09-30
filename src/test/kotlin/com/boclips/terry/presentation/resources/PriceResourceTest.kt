package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.Price
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Currency

class PriceResourceTest {
    @Test
    fun `convert price with currency to price resource`() {
        val price = Price(amount = BigDecimal.valueOf(1.12), currency = Currency.getInstance("GBP"))
        val priceResource = PriceResource.fromPrice(price)!!

        assertThat(priceResource.value).isEqualTo(BigDecimal.valueOf(1.12))
        assertThat(priceResource.currency).isEqualTo(Currency.getInstance("GBP"))
        assertThat(priceResource.displayValue).isEqualTo("GBP 1.12")
    }

    @Test
    fun `convert price with no currency to price resource`() {
        val price = Price(amount = BigDecimal.valueOf(1.12), currency = null)
        val priceResource = PriceResource.fromPrice(price)!!

        assertThat(priceResource.value).isEqualTo(BigDecimal.valueOf(1.12))
        assertThat(priceResource.currency).isNull()
        assertThat(priceResource.displayValue).isEqualTo("1.12")
    }

    @Test
    fun `convert a price without an amount`() {
        val price = Price(null, null)
        assertThat(PriceResource.fromPrice(price)).isNull()
    }

    @Test
    fun `convert a price with just currency`() {
        val price = Price(amount = null, currency = Currency.getInstance("USD"))
        assertThat(PriceResource.fromPrice(price)).isNull()
    }

    @Test
    fun `convert price with lots of decimal places to price resource`() {
        val price = Price(amount = BigDecimal.valueOf(1.1212321321), currency = Currency.getInstance("USD"))
        val priceResource = PriceResource.fromPrice(price)!!

        assertThat(priceResource.value).isEqualTo(BigDecimal.valueOf(1.12))
        assertThat(priceResource.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(priceResource.displayValue).isEqualTo("USD 1.12")
    }
}
