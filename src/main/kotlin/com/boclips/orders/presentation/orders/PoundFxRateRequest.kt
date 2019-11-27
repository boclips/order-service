package com.boclips.orders.presentation.orders

import java.math.BigDecimal

data class PoundFxRateRequest(
    val eur: BigDecimal,
    val usd: BigDecimal,
    val aud: BigDecimal,
    val sgd: BigDecimal,
    val cad: BigDecimal
)
