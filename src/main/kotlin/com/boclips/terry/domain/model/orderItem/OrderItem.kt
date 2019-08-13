package com.boclips.terry.domain.model.orderItem

import java.math.BigDecimal

data class OrderItem(
    val uuid: String,
    val price: BigDecimal,
    val transcriptRequested: Boolean,
    val video: Video,
    val contentPartner: ContentPartner
)
