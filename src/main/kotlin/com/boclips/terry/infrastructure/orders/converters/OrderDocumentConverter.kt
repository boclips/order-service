package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.infrastructure.orders.OrderDocument
import com.boclips.terry.infrastructure.orders.converters.OrderItemDocumentConverter.toOrderItem
import org.bson.types.ObjectId

object OrderDocumentConverter {
    fun toOrderDocument(order: Order): OrderDocument {
        return OrderDocument(
            id = ObjectId(order.id.value),
            orderProviderId = order.orderProviderId,
            status = order.status.toString(),
            authorisingUser = OrderUserDocumentConverter.toOrderUserDocument(orderUser = order.authorisingUser),
            requestingUser = OrderUserDocumentConverter.toOrderUserDocument(orderUser = order.requestingUser),
            updatedAt = order.updatedAt,
            createdAt = order.createdAt,
            isbnOrProductNumber = order.isbnOrProductNumber,
            items = order.items.map(OrderItemDocumentConverter::toOrderItemDocument)

        )
    }

    fun toOrder(document: OrderDocument): Order {
        return Order(
            id = OrderId(document.id.toHexString()),
            orderProviderId = document.orderProviderId,
            status = OrderStatus.valueOf(document.status),
            authorisingUser = OrderUserDocumentConverter.toOrderUser(document.authorisingUser),
            requestingUser = OrderUserDocumentConverter.toOrderUser(document.requestingUser),
            createdAt = document.createdAt,
            updatedAt = document.updatedAt,
            isbnOrProductNumber = document.isbnOrProductNumber,
            items = document.items?.map(OrderItemDocumentConverter::toOrderItem) ?: emptyList()
        )
    }
}
