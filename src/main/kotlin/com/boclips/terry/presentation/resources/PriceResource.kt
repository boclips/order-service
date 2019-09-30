package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.Price
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
