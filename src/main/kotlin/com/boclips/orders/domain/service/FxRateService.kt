package com.boclips.orders.domain.service

import com.boclips.orders.domain.exceptions.IllegalCurrencyException
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

@Component
class FxRateService {
    fun resolve(fxRates: Map<Currency, BigDecimal>, from: Currency, to: Currency): BigDecimal {
        val toFxRate = fxRates[to]
            ?: throw IllegalCurrencyException("Currency fx rate missing: ${to.currencyCode}. Cannot determine the fx rate")
        val fromFxRate = fxRates[from]
            ?: throw IllegalCurrencyException("Currency fx rate missing: ${from.currencyCode}. Cannot determine the fx rate")

        return toFxRate.divide(fromFxRate, 5, RoundingMode.HALF_UP)
    }
}
