package com.boclips.terry.domain.model

import java.math.BigDecimal
import java.util.Currency

sealed class Price(val value: BigDecimal) {
    class WithCurrency(value: BigDecimal, val currency: Currency) : Price(value)
    class WithoutCurrency(value: BigDecimal) : Price(value)
    object InvalidPrice : Price(BigDecimal.valueOf(-1.0))
}