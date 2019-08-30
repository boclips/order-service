package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.infrastructure.orders.ContentPartnerDocument
import com.boclips.terry.infrastructure.orders.LicenseDocument
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
                    videoServiceContentPartnerId = it.contentPartner.videoServiceId.value,
                    name = it.contentPartner.name
                ),
                videoReference = it.video.videoReference
            ),
            trim = toTrimmingString(it.trim),
            video = VideoDocument(
                videoServiceId = it.video.videoServiceId.value,
                title = it.video.title,
                type = it.video.type
            ),
            license = LicenseDocument(
                amount = it.license.duration.amount,
                unit = it.license.duration.unit,
                territory = it.license.territory
            )
        )
    }

    fun toOrderItem(it: OrderItemDocument): OrderItem {
        return OrderItem(
            uuid = it.uuid,
            price = it.price,
            transcriptRequested = it.transcriptRequested,
            contentPartner = ContentPartner(
                videoServiceId = ContentPartnerId(value = it.source.contentPartner.videoServiceContentPartnerId),
                name = it.source.contentPartner.name
            ),
            trim = it.trim?.let { TrimRequest.WithTrimming(it) } ?: TrimRequest.NoTrimming,
            video = Video(
                videoServiceId = VideoId(value = it.video.videoServiceId),
                title = it.video.title,
                type = it.video.type,
                videoReference = it.source.videoReference
            ),
            license = OrderItemLicense(
                duration = Duration(amount = it.license.amount, unit = it.license.unit),
                territory = it.license.territory
            )
        )
    }

    private fun toTrimmingString(
        trim: TrimRequest
    ): String? {
        return when (trim) {
            is TrimRequest.WithTrimming -> trim.label
            TrimRequest.NoTrimming -> null
        }
    }
}
