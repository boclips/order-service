package com.boclips.terry.infrastructure.orders

import com.boclips.eventbus.events.order.LegacyOrder
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderUser
import com.boclips.terry.domain.model.OrderId
import org.bson.codecs.pojo.annotations.BsonId

data class LegacyOrderDocument(
    val order: LegacyOrder,
    val items: List<LegacyOrderItem>,
    val requestingUser: LegacyOrderUser?,
    val authorisingUser: LegacyOrderUser?
)
