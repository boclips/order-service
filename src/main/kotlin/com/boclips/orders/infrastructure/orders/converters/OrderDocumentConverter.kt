package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.infrastructure.orders.OrderDocument
import org.bson.types.ObjectId

object OrderDocumentConverter {
    fun toOrderDocument(order: Order): OrderDocument {
        return OrderDocument(
            id = ObjectId(order.id.value),
            legacyOrderId = order.legacyOrderId,
            status = order.status.toString(),
            authorisingUser = order.authorisingUser?.let { OrderUserDocumentConverter.toOrderUserDocument(orderUser = it) },
            requestingUser = OrderUserDocumentConverter.toOrderUserDocument(orderUser = order.requestingUser),
            updatedAt = order.updatedAt,
            createdAt = order.createdAt,
            isbnOrProductNumber = order.isbnOrProductNumber,
            items = order.items.map(OrderItemDocumentConverter::toOrderItemDocument),
            organisation = order.organisation?.name,
            orderThroughPlatform = order.isThroughPlatform,
            currency = order.currency,
            fxRateToGbp = order.fxRateToGbp
        )
    }

    fun toOrder(document: OrderDocument): Order {
        return Order(
            id = OrderId(document.id.toHexString()),
            legacyOrderId = document.legacyOrderId,
            status = convertOrderStatus(document),
            authorisingUser = document.authorisingUser?.let { OrderUserDocumentConverter.toOrderUser(it) },
            requestingUser = OrderUserDocumentConverter.toOrderUser(document.requestingUser),
            createdAt = document.createdAt,
            updatedAt = document.updatedAt,
            isbnOrProductNumber = document.isbnOrProductNumber,
            items = document.items?.map { OrderItemDocumentConverter.toOrderItem(it, document) } ?: emptyList(),
            organisation = document.organisation?.let { OrderOrganisation(name = it) },
            isThroughPlatform = document.orderThroughPlatform,
            currency = document.currency,
            fxRateToGbp = document.fxRateToGbp
        )
    }

    private fun convertOrderStatus(document: OrderDocument): OrderStatus {
        if (document.status == "COMPLETED") {
            return OrderStatus.READY
        }

        return try {
            OrderStatus.valueOf(document.status)
        } catch (e: IllegalArgumentException) {
            OrderStatus.INVALID
        }
    }
}
