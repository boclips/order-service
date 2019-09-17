package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.domain.model.Price
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Currency

class PriceFieldConverterTest {
    val priceFieldConverter = PriceFieldConverter

    @Test
    fun `converts $ to USD dollars`() {
        val price = priceFieldConverter.convert("$100")
        assertThat(price.amount!!.toDouble()).isEqualTo(100.0)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts € to euros`() {
        val price = priceFieldConverter.convert("€100")
        assertThat(price.amount!!.toDouble()).isEqualTo(100.0)
        assertThat(price.currency).isEqualTo(Currency.getInstance("EUR"))
    }

    @Test
    fun `still parses price when no currency present`() {
        val price = priceFieldConverter.convert("98.12")
        assertThat(price.amount!!.toDouble()).isEqualTo(98.12)
    }

    @Test
    fun `returns invalid when no price present`() {
        val price = priceFieldConverter.convert("")
        assertThat(price).isEqualTo(Price(null, null))
    }

    @Test
    fun `returns correct currency even with spaces`() {
        val price = priceFieldConverter.convert("$  100")
        assertThat(price.amount!!.toDouble()).isEqualTo(100.0)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts string with CAD $ to CAD dollars`() {
        val price = priceFieldConverter.convert("CAD $149.21")

        assertThat(price.amount!!.toDouble()).isEqualTo(149.21)
        assertThat(price.currency).isEqualTo(Currency.getInstance("CAD"))
    }

    @Test
    fun `converts string with USD $ to USD dollars`() {
        val price = priceFieldConverter.convert("USD $149.21")

        assertThat(price.amount!!.toDouble()).isEqualTo(149.21)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts string with USD to USD dollars`() {
        val price = priceFieldConverter.convert("USD149.21")

        assertThat(price.amount!!.toDouble()).isEqualTo(149.21)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts string with US$ to USD dollars`() {
        val price = priceFieldConverter.convert("US$199.50")

        assertThat(price.amount!!.toDouble()).isEqualTo(199.5)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }


    @Test
    fun `converts string with EUR to euros`() {
        val price = priceFieldConverter.convert("EUR 199.50")

        assertThat(price.amount!!.toDouble()).isEqualTo(199.5)
        assertThat(price.currency).isEqualTo(Currency.getInstance("EUR"))
    }

    @Test
    fun `extracts price with no currency when the currency can not be determined`() {
        val price = priceFieldConverter.convert("RTYHJ 111.23")
        assertThat(price.amount!!.toDouble()).isEqualTo(111.23)
        assertThat(price.currency).isNull()
    }
}
