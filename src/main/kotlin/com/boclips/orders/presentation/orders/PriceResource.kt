package com.boclips.orders.presentation.orders

import com.boclips.orders.domain.model.Price
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency

data class PriceResource(
    val value: BigDecimal,
    val currency: Currency?
) {
    companion object {
        fun fromPrice(price: Price): PriceResource? {
            return price.amount?.let { amount ->
                PriceResource(
                    value = amount,
                    currency = price.currency
                )
            }
        }
    }

    val displayValue: String
        get() = (currency?.let { "$it " } ?: "") + DecimalFormat("0.00").format(value)
}
