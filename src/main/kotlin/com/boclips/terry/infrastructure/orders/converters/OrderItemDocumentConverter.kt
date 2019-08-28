package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.infrastructure.orders.ContentPartnerDocument
import com.boclips.terry.infrastructure.orders.OrderItemDocument
import com.boclips.terry.infrastructure.orders.SourceDocument
import com.boclips.terry.infrastructure.orders.VideoDocument

object OrderItemDocumentConverter {
    fun toOrderItemDocument(it: OrderItem): OrderItemDocument {
        return OrderItemDocument(
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

    fun toOrderItem(it: OrderItemDocument): OrderItem {
        return OrderItem(
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
    }
}
