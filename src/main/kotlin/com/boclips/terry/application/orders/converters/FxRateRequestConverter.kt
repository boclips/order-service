package com.boclips.terry.application.orders.converters

import com.boclips.terry.presentation.orders.PoundFxRateRequest
import java.math.BigDecimal
import java.util.Currency

object FxRateRequestConverter {
    fun convert(request: PoundFxRateRequest): Map<Currency, BigDecimal> {
        return mapOf(
            Currency.getInstance("EUR") to request.eur,
            Currency.getInstance("USD") to request.usd,
            Currency.getInstance("SGD") to request.sgd,
            Currency.getInstance("AUD") to request.aud,
            Currency.getInstance("GBP") to BigDecimal.ONE
        )
    }

}
