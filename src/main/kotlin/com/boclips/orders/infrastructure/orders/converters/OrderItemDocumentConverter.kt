package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.ContentPartner
import com.boclips.orders.domain.model.orderItem.ContentPartnerId
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItem
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.domain.model.orderItem.Video
import com.boclips.orders.domain.model.orderItem.VideoId
import com.boclips.orders.infrastructure.orders.ContentPartnerDocument
import com.boclips.orders.infrastructure.orders.LicenseDocument
import com.boclips.orders.infrastructure.orders.OrderItemDocument
import com.boclips.orders.infrastructure.orders.SourceDocument
import com.boclips.orders.infrastructure.orders.VideoDocument
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
            license = it.license?.duration?.let { duration ->
                when(duration) {
                    is Duration.Time -> LicenseDocument(
                        amount = duration.amount,
                        unit = duration.unit,
                        territory = it.license.territory,
                        description = null
                    )
                    is Duration.Description -> LicenseDocument(
                        amount = null,
                        unit = null,
                        territory = it.license.territory,
                        description = duration.label
                    )
                }
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
            license = document.license?.let { license ->
                OrderItemLicense(
                        duration = when {
                            license.isValidTime() -> Duration.Time(
                                    amount = license.amount!!,
                                    unit = license.unit!!
                            )
                            license.isValidDescription() -> Duration.Description(label = license.description!!)
                            else -> throw IllegalStateException("Invalid duration")
                        },
                        territory = license.territory
                )
            },
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
