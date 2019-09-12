package com.boclips.terry.domain.model

import java.util.Currency

sealed class Price(val value: Double) {
    class WithCurrency(value: Double, val currency: Currency) : Price(value)
    class WithoutCurrency(value: Double) : Price(value)
    object InvalidPrice : Price(-1.0)
}