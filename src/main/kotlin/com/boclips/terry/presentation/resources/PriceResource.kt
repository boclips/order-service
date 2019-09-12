package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.Price
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class PriceResource(
    val value: BigDecimal,
    val displayValue: String,
    val currency: Currency?
) {
    companion object {
        fun fromPrice(price: Price): PriceResource? {
            return when (price) {
                is Price.WithCurrency -> PriceResource(
                    value = price.value,
                    displayValue = "${price.currency.currencyCode} ${DecimalFormat("0.00").format(price.value)}",
                    currency = price.currency
                )
                is Price.WithoutCurrency -> PriceResource(
                    value = price.value,
                    displayValue = DecimalFormat("0.00").format(price.value),
                    currency = null
                )
                Price.InvalidPrice -> null
            }
        }
    }
}
