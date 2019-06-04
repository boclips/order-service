package com.boclips.terry.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.LegacyOrderSubmitted
import com.boclips.terry.application.exceptions.LegacyOrderProcessingException
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Component
import java.lang.IllegalStateException

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
                    id = OrderId(event.order.id),
                    uuid = event.order.uuid,
                    createdAt = event.order.dateCreated.toInstant(),
                    updatedAt = event.order.dateUpdated.toInstant(),
                    creatorEmail = event.creator,
                    vendorEmail = event.vendor,
                    isbnOrProductNumber = event.order.extraFields.isbnOrProductNumber,
                    status = OrderStatus.parse(event.order.status),
                    items = event.orderItems.map {
                        OrderItem(
                            uuid = it.uuid,
                            price = it.price,
                            transcriptRequested = it.transcriptsRequired
                        )
                    }
                ),
                legacyDocument = LegacyOrderDocument(
                    order = event.order,
                    items = event.orderItems,
                    creator = event.creator,
                    vendor = event.vendor
                )
            )
        } catch (e: IllegalStateException) {
            logger.error { "Couldn't process legacy order: $e" }
        } catch (e: Exception) {
            logger.error { "Couldn't process legacy order: $e" }
            if (e !is IllegalStateException) {
                throw LegacyOrderProcessingException(e)
            }
        }
    }
}
