package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.Video
import com.boclips.terry.domain.model.VideoId
import com.boclips.terry.infrastructure.orders.OrderDocument
import com.boclips.terry.infrastructure.orders.OrderItemDocument
import com.boclips.terry.infrastructure.orders.VideoDocument
import org.bson.types.ObjectId

object OrderDocumentConverter {
    fun toOrderDocument(order: Order): OrderDocument {
        return OrderDocument(
            id = ObjectId(order.id.value),
            uuid = order.uuid,
            status = order.status.toString(),
            vendorEmail = order.vendorEmail,
            creatorEmail = order.creatorEmail,
            updatedAt = order.updatedAt,
            createdAt = order.createdAt,
            isbnOrProductNumber = order.isbnOrProductNumber,
            items = order.items.map {
                OrderItemDocument(
                    uuid = it.uuid,
                    price = it.price,
                    transcriptRequested = it.transcriptRequested,
                    video = VideoDocument(
                        id = it.video.id.value,
                        title = it.video.title,
                        source = it.video.source,
                        type = it.video.type
                    )
                )
            }

        )
    }

    fun toOrder(document: OrderDocument): Order {
        return Order(
            id = OrderId(document.id.toHexString()),
            uuid = document.uuid,
            status = OrderStatus.valueOf(document.status),
            vendorEmail = document.vendorEmail,
            creatorEmail = document.creatorEmail,
            createdAt = document.createdAt,
            updatedAt = document.updatedAt,
            isbnOrProductNumber = document.isbnOrProductNumber,
            items = document.items?.map {
                OrderItem(
                    uuid = it.uuid,
                    price = it.price,
                    transcriptRequested = it.transcriptRequested,
                    video = Video(
                        id = VideoId(it.video.id),
                        title = it.video.title,
                        source = it.video.source,
                        type = it.video.type
                    )
                )
            } ?: emptyList()
        )
    }
}
