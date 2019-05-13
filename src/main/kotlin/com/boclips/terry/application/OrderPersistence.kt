package com.boclips.terry.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.LegacyOrderSubmitted
import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.domain.OrdersRepository
import com.boclips.terry.infrastructure.LegacyOrderDocument
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Component

@Component
class OrderPersistence(
    private val repo: OrdersRepository
) {
    companion object : KLogging()

    @StreamListener(Subscriptions.LEGACY_ORDER_SUBMITTED)
    fun onLegacyOrderSubmitted(event: LegacyOrderSubmitted) {
        try {
            repo.add(
                order = Order(
                    id = event.order.id,
                    uuid = event.order.uuid,
                    createdAt = event.order.dateCreated.toInstant(),
                    updatedAt = event.order.dateUpdated.toInstant(),
                    creator = event.order.creator,
                    vendor = event.order.vendor,
                    isbnOrProductNumber = event.order.extraFields.isbnOrProductNumber,
                    status = OrderStatus.parse(event.order.status)
                ),
                legacyDocument = LegacyOrderDocument(
                    order = event.order,
                    items = event.orderItems
                )
            )
        } catch (e: Exception) {
            logger.error { "Couldn't process legacy order: $e" }
        }
    }
}
