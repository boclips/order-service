package com.boclips.orders.domain.service.currency

import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

interface FxRateService {
    fun resolve(from: Currency, to: Currency, on: LocalDate): BigDecimal
}