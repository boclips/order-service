package com.boclips.terry.infrastructure.orders

import java.math.BigDecimal

data class OrderItemDocument(
    val uuid: String,
    val price: BigDecimal,
    val transcriptRequested: Boolean
)