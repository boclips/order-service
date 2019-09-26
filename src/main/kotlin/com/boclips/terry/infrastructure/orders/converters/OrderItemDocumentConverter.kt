package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.Price
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
import java.util.Currency

object OrderItemDocumentConverter {
    fun toOrderItemDocument(it: OrderItem): OrderItemDocument {
        return OrderItemDocument(
            price = it.price.amount,
            currency = it.price.currency,
            transcriptRequested = it.transcriptRequested,
            source = SourceDocument(
                contentPartner = ContentPartnerDocument(
                    videoServiceContentPartnerId = it.video.contentPartner.videoServiceId.value,
                    name = it.video.contentPartner.name,
                    currency = it.video.contentPartner.currency.currencyCode
                ),
                videoReference = it.video.contentPartnerVideoId
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
            },
            notes = it.notes,
            id = it.id
        )
    }

    fun toOrderItem(document: OrderItemDocument): OrderItem {
        return OrderItem(
            id = document.id,
            price = Price(document.price, document.currency),
            transcriptRequested = document.transcriptRequested,

            trim = document.trim?.let { TrimRequest.WithTrimming(it) } ?: TrimRequest.NoTrimming,
            video = Video(
                videoServiceId = VideoId(value = document.video.videoServiceId),
                title = document.video.title,
                type = document.video.type,
                contentPartnerVideoId = document.source.videoReference,
                contentPartner = ContentPartner(
                    videoServiceId = ContentPartnerId(value = document.source.contentPartner.videoServiceContentPartnerId),
                    name = document.source.contentPartner.name,
                    currency = Currency.getInstance(document.source.contentPartner.currency)
                )
            ),
            license = OrderItemLicense(
                duration = when {
                    document.license.isValidTime() -> Duration.Time(
                        amount = document.license.amount!!,
                        unit = document.license.unit!!
                    )
                    document.license.isValidDescription() -> Duration.Description(label = document.license.description!!)
                    else -> throw IllegalStateException("Invalid duration")
                },
                territory = document.license.territory
            ),
            notes = document.notes
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
