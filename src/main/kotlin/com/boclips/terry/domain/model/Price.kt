package com.boclips.terry.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

class Price(
    amount: BigDecimal?,
    val currency: Currency?
) {
    val amount: BigDecimal? = amount?.setScale(2, RoundingMode.HALF_UP)

    fun toReadableString(): String = "${currency?.currencyCode} $amount"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Price

        if (amount != other.amount) return false
        if (currency != other.currency) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount?.hashCode() ?: 0
        result = 31 * result + (currency?.hashCode() ?: 0)
        return result
    }
}
