package com.boclips.orders.infrastructure.orders

import com.boclips.eventbus.events.order.LegacyOrder
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderUser

data class LegacyOrderDocument(
    val order: LegacyOrder,
    val items: List<LegacyOrderItem>,
    val requestingUser: LegacyOrderUser?,
    val authorisingUser: LegacyOrderUser?
)
