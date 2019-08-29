package com.boclips.terry.domain.model.orderItem

sealed class TrimRequest {
    data class WithTrimming(val label: String) : TrimRequest()
    object NoTrimming : TrimRequest()
}
