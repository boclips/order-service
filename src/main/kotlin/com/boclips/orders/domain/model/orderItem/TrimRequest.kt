package com.boclips.orders.domain.model.orderItem

import com.boclips.orders.presentation.TrimServiceRequest

sealed class TrimRequest {
    data class WithTrimming(val label: String) : TrimRequest()
    object NoTrimming : TrimRequest()

    companion object {
        fun fromTrimServiceRequest(trimServiceRequest: TrimServiceRequest?) =
            trimServiceRequest?.let { it -> TrimRequest.WithTrimming(label = "${it.from} - ${it.to}") }
                ?: TrimRequest.NoTrimming
    }
}
