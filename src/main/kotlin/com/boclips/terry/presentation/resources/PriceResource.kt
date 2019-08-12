package com.boclips.terry.presentation.resources

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class PriceResource(
    val value: BigDecimal,
    val displayValue: String
) {
    companion object {
        fun fromBigDecimal(bigDecimal: BigDecimal): PriceResource {
            return PriceResource(
                value = bigDecimal,
                displayValue = NumberFormat.getCurrencyInstance(Locale.US).format(bigDecimal)
            )
        }
    }
}
