package com.boclips.terry.domain.model

import java.math.BigDecimal
import java.util.Currency

data class Price(
    val amount: BigDecimal?,
    val currency: Currency?
) {
    fun toReadableString(): String = "${currency?.currencyCode} $amount"
}
