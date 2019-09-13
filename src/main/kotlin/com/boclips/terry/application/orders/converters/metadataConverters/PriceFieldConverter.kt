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

    private val doubleRegex: Regex = Regex.fromLiteral("/^[0-9]+(\\.[0-9]+)?$")

    fun convert(unparsedPrice: String): Price {
        delimiterToCurrency.map { delimiterToCurrencyPair ->
            unparsedPrice.contains(delimiterToCurrencyPair.key)
                .takeIf { it }
                ?.let { unparsedPrice.replace(delimiterToCurrencyPair.key, "") }
                ?.let { it.toDoubleOrNull() }
                ?.let { return Price(BigDecimal.valueOf(it), currency = delimiterToCurrencyPair.value) }
        }


        return unparsedPrice.toDoubleOrNull()?.let { Price(amount = BigDecimal.valueOf(it), currency = null) }
            ?: Price(amount = null, currency = null)
    }
}
