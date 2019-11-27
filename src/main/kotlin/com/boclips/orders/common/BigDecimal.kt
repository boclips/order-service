package com.boclips.orders.common

import java.math.BigDecimal

fun Iterable<BigDecimal>.sumByBigDecimal(): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, e -> acc + e }
}

fun <T> Iterable<T>.sumByBigDecimal(transform: (T) -> BigDecimal): BigDecimal {
    return this.fold(BigDecimal.ZERO) { acc, e -> acc + transform.invoke(e) }
}
