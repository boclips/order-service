package com.boclips.orders.application.orders.converters.csv

import com.boclips.orders.application.orders.converters.csv.PriceParser.delimiterToCurrency
import com.boclips.orders.application.orders.converters.csv.PriceParser.doubleRegex
import com.boclips.orders.domain.model.Price
import java.math.BigDecimal
import java.util.Currency

private object PriceParser {
    val delimiterToCurrency: Map<String, Currency> = mapOf(
        "CAD $" to Currency.getInstance("CAD"),
        "USD $" to Currency.getInstance("USD"),
        "USD" to Currency.getInstance("USD"),
        "US$" to Currency.getInstance("USD"),
        "$" to Currency.getInstance("USD"),
        "EUR" to Currency.getInstance("EUR"),
        "€" to Currency.getInstance("EUR")
    ) // these are in priority order

    val doubleRegex: Regex = """(-?[0-9]+(?:[,.][0-9]+)?)""".toRegex()
}

fun String?.parsePrice(): Price {
    if (this == null) {
        return Price(null, null)
    }

    val amount = doubleRegex.find(this)?.value?.toDoubleOrNull()
        ?.let { amount -> BigDecimal.valueOf(amount) }


    delimiterToCurrency.forEach { delimiterToCurrencyPair ->
        if (this.contains(delimiterToCurrencyPair.key)) {
            return Price(amount = amount, currency = delimiterToCurrencyPair.value)
        }
    }

    return Price(amount = amount, currency = null)
}
