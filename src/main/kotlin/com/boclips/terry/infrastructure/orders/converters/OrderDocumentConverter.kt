package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.infrastructure.orders.ContentPartnerDocument
import com.boclips.terry.infrastructure.orders.OrderDocument
import com.boclips.terry.infrastructure.orders.OrderItemDocument
import com.boclips.terry.infrastructure.orders.SourceDocument
import com.boclips.terry.infrastructure.orders.VideoDocument
import org.bson.types.ObjectId

object OrderDocumentConverter {
    fun toOrderDocument(order: Order): OrderDocument {
        return OrderDocument(
            id = ObjectId(order.id.value),
            orderProviderId = order.orderProviderId,
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
                    source = SourceDocument(
                        contentPartner = ContentPartnerDocument(
                            referenceId = it.contentPartner.referenceId.value,
                            name = it.contentPartner.name
                        ),
                        videoReference = it.video.videoReference
                    ),
                    video = VideoDocument(
                        referenceId = it.video.referenceId.value,
                        title = it.video.title,
                        type = it.video.type
                    )
                )
            }

        )
    }

    fun toOrder(document: OrderDocument): Order {
        return Order(
            id = OrderId(document.id.toHexString()),
            orderProviderId = document.orderProviderId,
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
                    contentPartner = ContentPartner(
                        referenceId = ContentPartnerId(value = it.source.contentPartner.referenceId),
                        name = it.source.contentPartner.name
                    ),
                    video = Video(
                        referenceId = VideoId(value = it.video.referenceId),
                        title = it.video.title,
                        type = it.video.type,
                        videoReference = it.source.videoReference
                    )
                )
            } ?: emptyList()
        )
    }
}
