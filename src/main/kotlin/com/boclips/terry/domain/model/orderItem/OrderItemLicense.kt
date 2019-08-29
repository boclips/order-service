package com.boclips.terry.domain.model.orderItem


data class OrderItemLicense(val duration: Duration, val territory: Territory)

enum class Territory {
    SINGLE_REGION,
    MULTI_REGION,
    WORLDWIDE
}
