package com.boclips.terry.domain.model

import java.math.BigDecimal

data class OrderItem(
    val uuid: String,
    val price: BigDecimal,
    val transcriptRequested: Boolean
)