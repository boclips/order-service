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
            price = it.price,
            transcriptRequested = it.transcriptRequested,
            source = SourceDocument(
                contentPartner = ContentPartnerDocument(
                    videoServiceContentPartnerId = it.video.contentPartner.videoServiceId.value,
                    name = it.video.contentPartner.name
                ),
                videoReference = it.video.videoReference
            ),
            trim = toTrimmingString(it.trim),
            video = VideoDocument(
                videoServiceId = it.video.videoServiceId.value,
                title = it.video.title,
                type = it.video.type
            ),
            license = when (it.license.duration) {
                is Duration.Time -> LicenseDocument(
                    amount = it.license.duration.amount,
                    unit = it.license.duration.unit,
                    territory = it.license.territory,
                    description = null
                )
                is Duration.Description -> LicenseDocument(
                    amount = null,
                    unit = null,
                    territory = it.license.territory,
                    description = it.license.duration.label
                )
            }
        )
    }

    fun toOrderItem(it: OrderItemDocument): OrderItem {
        return OrderItem(
            price = it.price,
            transcriptRequested = it.transcriptRequested,

            trim = it.trim?.let { TrimRequest.WithTrimming(it) } ?: TrimRequest.NoTrimming,
            video = Video(
                videoServiceId = VideoId(value = it.video.videoServiceId),
                title = it.video.title,
                type = it.video.type,
                videoReference = it.source.videoReference,
                contentPartner = ContentPartner(
                    videoServiceId = ContentPartnerId(value = it.source.contentPartner.videoServiceContentPartnerId),
                    name = it.source.contentPartner.name
                )
            ),
            license = OrderItemLicense(
                duration = when {
                    it.license.isValidTime() -> Duration.Time(
                        amount = it.license.amount!!,
                        unit = it.license.unit!!
                    )
                    it.license.isValidDescription() -> Duration.Description(label = it.license.description!!)
                    else -> throw IllegalStateException("Invalid duration")
                },
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
