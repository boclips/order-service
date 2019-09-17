package com.boclips.terry.domain.model.orderItem

import com.boclips.terry.domain.model.Price

data class OrderItem(
    val price: Price,
    val transcriptRequested: Boolean,
    val trim: TrimRequest,
    val video: Video,
    val license: OrderItemLicense,
    val notes: String?
)
