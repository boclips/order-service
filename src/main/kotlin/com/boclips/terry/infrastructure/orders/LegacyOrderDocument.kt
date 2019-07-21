package com.boclips.terry.infrastructure.orders

import com.boclips.eventbus.events.order.LegacyOrder
import com.boclips.eventbus.events.order.LegacyOrderItem

data class LegacyOrderDocument(
    val order: LegacyOrder,
    val items: List<LegacyOrderItem>,
    val creator: String,
    val vendor: String
)
