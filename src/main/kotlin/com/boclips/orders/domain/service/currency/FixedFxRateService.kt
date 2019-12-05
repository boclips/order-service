package com.boclips.orders.domain.service.currency

import com.boclips.orders.domain.exceptions.IllegalCurrencyException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.Currency

class FixedFxRateService(private val fxRates: Map<Currency, BigDecimal>) :
    FxRateService {
    override fun resolve(from: Currency, to: Currency, on: LocalDate): BigDecimal {
        return resolve(from, to)
    }

    fun resolve(from: Currency, to: Currency): BigDecimal {
        val toFxRate = fxRates[to]
            ?: throw IllegalCurrencyException("Currency fx rate missing: ${to.currencyCode}. Cannot determine the fx rate")
        val fromFxRate = fxRates[from]
            ?: throw IllegalCurrencyException("Currency fx rate missing: ${from.currencyCode}. Cannot determine the fx rate")

        return toFxRate.divide(fromFxRate, 5, RoundingMode.HALF_UP)
    }
}
