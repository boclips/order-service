package com.boclips.terry.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BigDecimalKtTest {

    @Test
    fun sumByBigDecimal() {
        val bigDecimals = listOf(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN)

        assertThat(bigDecimals.sumByBigDecimal()).isEqualTo(BigDecimal.valueOf(11L))
    }

    @Test
    fun sumByBigDecimalWithTransform() {
        data class Cost(val amount: BigDecimal?)

        val costs = listOf(Cost(amount = BigDecimal.valueOf(100)), Cost(amount = null))

        assertThat(costs.sumByBigDecimal { it.amount ?: BigDecimal.ZERO }).isEqualTo(BigDecimal.valueOf(100))
    }
}
