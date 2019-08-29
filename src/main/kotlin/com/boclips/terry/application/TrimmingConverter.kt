package com.boclips.terry.application

import com.boclips.terry.domain.model.orderItem.TrimRequest

object TrimmingConverter {
    fun toTrimRequest(label: String?): TrimRequest {
        return if (label.isNullOrBlank()) TrimRequest.NoTrimming else TrimRequest.WithTrimming(label)
    }
}
