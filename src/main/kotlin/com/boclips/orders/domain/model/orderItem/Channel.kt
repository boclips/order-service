package com.boclips.orders.domain.model.orderItem

import java.util.Currency

data class Channel(
    val videoServiceId: ChannelId,
    val name: String,
    val currency: Currency
)
