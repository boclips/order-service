package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.domain.model.Price
import java.math.BigDecimal
import java.util.Currency

object PriceFieldConverter {
    private val delimiterToCurrency: Map<String, Currency> = mapOf(
        "CAD $" to Currency.getInstance("CAD"),
        "USD $" to Currency.getInstance("USD"),
        "USD" to Currency.getInstance("USD"),
        "US$" to Currency.getInstance("USD"),
        "$" to Currency.getInstance("USD"),
        "EUR" to Currency.getInstance("EUR"),
        "€" to Currency.getInstance("EUR")
    ) // these are in priority order

    private val doubleRegex: Regex = """(-?[0-9]+(?:[,.][0-9]+)?)""".toRegex()

    fun convert(unparsedPrice: String): Price {
        val amount = doubleRegex.find(unparsedPrice)?.value?.toDoubleOrNull()
            ?.let { BigDecimal.valueOf(it) }

        delimiterToCurrency.forEach { delimiterToCurrencyPair ->
            if (unparsedPrice.contains(delimiterToCurrencyPair.key)) {
                return Price(amount = amount, currency = delimiterToCurrencyPair.value)
            }
        }


        return Price(amount = amount, currency = null)
    }
}
