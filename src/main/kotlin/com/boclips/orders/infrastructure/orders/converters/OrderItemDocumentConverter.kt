package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.*
import com.boclips.orders.infrastructure.orders.*
import java.net.URL
import java.util.*

object OrderItemDocumentConverter {
    fun toOrderItemDocument(it: OrderItem): OrderItemDocument {
        return OrderItemDocument(
            price = it.price.amount,
            transcriptRequested = it.transcriptRequested,
            source = SourceDocument(
                channel = ChannelDocument(
                    videoServiceChannelId = it.video.channel.videoServiceId.value,
                    name = it.video.channel.name,
                    currency = it.video.channel.currency.currencyCode
                ),
                videoReference = it.video.channelVideoId
            ),
            trim = toTrimmingString(it.trim),
            video = VideoDocument(
                videoServiceId = it.video.videoServiceId.value,
                title = it.video.title,
                type = it.video.type,
                fullProjectionLink = it.video.fullProjectionLink.toString()
            ),
            license = it.license?.duration?.let { duration ->
                when (duration) {
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

    fun toOrderItem(document: OrderItemDocument, orderDocument: OrderDocument): OrderItem {
        return OrderItem(
            id = document.id,
            price = Price(document.price, orderDocument.currency),
            transcriptRequested = document.transcriptRequested,

            trim = document.trim?.let { TrimRequest.WithTrimming(it) } ?: TrimRequest.NoTrimming,
            video = Video(
                videoServiceId = VideoId(value = document.video.videoServiceId),
                title = document.video.title,
                type = document.video.type,
                channelVideoId = document.source.videoReference,
                channel = Channel(
                    videoServiceId = ChannelId(value = document.source.channel.videoServiceChannelId),
                    name = document.source.channel.name,
                    currency = Currency.getInstance(document.source.channel.currency)
                ),
                fullProjectionLink = URL(document.video.fullProjectionLink)
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
