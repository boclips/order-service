package com.boclips.terry.application.orders.converters.csv

import com.boclips.terry.domain.model.Price
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Currency

class PriceParserKtTest {

    @Test
    fun `converts $ to USD dollars`() {
        val price = "$100".parsePrice()
        assertThat(price.amount!!.toDouble()).isEqualTo(100.0)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts € to euros`() {
        val price = "€100".parsePrice()
        assertThat(price.amount!!.toDouble()).isEqualTo(100.0)
        assertThat(price.currency).isEqualTo(Currency.getInstance("EUR"))
    }

    @Test
    fun `still parses price when no currency present`() {
        val price = "98.12".parsePrice()
        assertThat(price.amount!!.toDouble()).isEqualTo(98.12)
    }

    @Test
    fun `returns invalid when no price present`() {
        val price = "".parsePrice()
        assertThat(price).isEqualTo(Price(null, null))
    }

    @Test
    fun `returns correct currency even with spaces`() {
        val price = "$  100".parsePrice()
        assertThat(price.amount!!.toDouble()).isEqualTo(100.0)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts string with CAD $ to CAD dollars`() {
        val price = "CAD $149.21".parsePrice()

        assertThat(price.amount!!.toDouble()).isEqualTo(149.21)
        assertThat(price.currency).isEqualTo(Currency.getInstance("CAD"))
    }

    @Test
    fun `converts string with USD $ to USD dollars`() {
        val price = "USD $149.21".parsePrice()

        assertThat(price.amount!!.toDouble()).isEqualTo(149.21)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts string with USD to USD dollars`() {
        val price = "USD149.21".parsePrice()

        assertThat(price.amount!!.toDouble()).isEqualTo(149.21)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts string with US$ to USD dollars`() {
        val price = "US$199.50".parsePrice()

        assertThat(price.amount!!.toDouble()).isEqualTo(199.5)
        assertThat(price.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `converts string with EUR to euros`() {
        val price = "EUR 199.50".parsePrice()

        assertThat(price.amount!!.toDouble()).isEqualTo(199.5)
        assertThat(price.currency).isEqualTo(Currency.getInstance("EUR"))
    }

    @Test
    fun `extracts price with no currency when the currency can not be determined`() {
        val price = "RTYHJ 111.23".parsePrice()
        assertThat(price.amount!!.toDouble()).isEqualTo(111.23)
        assertThat(price.currency).isNull()
    }

    @Test
    fun `null input`() {
        val price = null.parsePrice()
        assertThat(price.amount).isNull()
        assertThat(price.currency).isNull()
    }
}
