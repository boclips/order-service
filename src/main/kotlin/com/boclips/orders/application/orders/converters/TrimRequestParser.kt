package com.boclips.orders.application.orders.converters

import com.boclips.orders.domain.model.orderItem.TrimRequest

fun String?.parseTrimRequest(): TrimRequest {
    return if (this.isNullOrBlank()) TrimRequest.NoTrimming else TrimRequest.WithTrimming(this)
}
