package com.boclips.terry.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.LegacyOrderSubmitted
import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrdersRepository
import com.boclips.terry.infrastructure.LegacyOrderDocument
import org.springframework.cloud.stream.annotation.StreamListener

class OrderPersistence(
    private val repo: OrdersRepository
) {
    @StreamListener(Subscriptions.LEGACY_ORDER_SUBMITTED)
    fun onLegacyOrderSubmitted(event: LegacyOrderSubmitted) {
        repo.add(
            order = Order(id = event.order.id),
            legacyDocument = LegacyOrderDocument(
                order = event.order,
                items = event.orderItems
            )
        )
    }
}
