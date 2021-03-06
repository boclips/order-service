package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.domain.exceptions.IllegalOrderStateException
import com.boclips.orders.domain.model.*
import com.boclips.orders.infrastructure.orders.OrderDocument
import mu.KLogging
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
            deliveredAt = order.deliveredAt,
            isbnOrProductNumber = order.isbnOrProductNumber,
            items = order.items.map(OrderItemDocumentConverter::toOrderItemDocument),
            organisation = order.organisation?.name,
            orderSource = order.orderSource.name,
            currency = order.currency,
            fxRateToGbp = order.fxRateToGbp,
            note = order.note
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
            deliveredAt = document.deliveredAt,
            isbnOrProductNumber = document.isbnOrProductNumber,
            items = document.items?.map { OrderItemDocumentConverter.toOrderItem(it, document) } ?: emptyList(),
            organisation = document.organisation?.let { OrderOrganisation(name = it) },
            orderSource = convertOrderSource(document),
            currency = document.currency,
            fxRateToGbp = document.fxRateToGbp,
            note = document.note
        )
    }

    private fun convertOrderSource(document: OrderDocument): OrderSource = try {
        document.orderSource.let { OrderSource.valueOf(it) }
    } catch (e: IllegalArgumentException) {
        KLogging().logger.error { "Illegal orderSource value ${document.orderSource} when converting from orderDocument of id ${document.id.toHexString()}" }
        throw IllegalOrderStateException(OrderId(document.id.toHexString()), "Illegal orderSource value: '${document.orderSource}'")
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
