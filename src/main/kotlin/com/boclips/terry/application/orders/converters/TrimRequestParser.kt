package com.boclips.terry.application.orders.converters

import com.boclips.terry.domain.model.orderItem.TrimRequest

fun String?.parseTrimRequest(): TrimRequest {
    return if (this.isNullOrBlank()) TrimRequest.NoTrimming else TrimRequest.WithTrimming(this)
}
