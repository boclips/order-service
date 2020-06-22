package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Channel
import com.boclips.orders.domain.model.orderItem.ChannelId
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItem
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.domain.model.orderItem.Video
import com.boclips.orders.domain.model.orderItem.VideoId
import com.boclips.orders.infrastructure.orders.ChannelDocument
import com.boclips.orders.infrastructure.orders.LicenseDocument
import com.boclips.orders.infrastructure.orders.OrderDocument
import com.boclips.orders.infrastructure.orders.OrderItemDocument
import com.boclips.orders.infrastructure.orders.SourceDocument
import com.boclips.orders.infrastructure.orders.VideoDocument
import java.net.URL
import java.util.Currency

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
                fullProjectionLink = it.video.fullProjectionLink.toString(),
                captionStatus = it.video.captionStatus.toString(),
                hasHDVideo = when (it.video.downloadableVideoStatus) {
                    AssetStatus.AVAILABLE -> true
                    AssetStatus.UNAVAILABLE -> false
                    else -> null
                },
                playbackId = it.video.playbackId
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
                fullProjectionLink = URL(document.video.fullProjectionLink),
                videoUploadLink = KalturaLinkConverter.getVideoUploadLink(document.video.playbackId),
                captionAdminLink = KalturaLinkConverter.getCaptionAdminLink(document.video.playbackId),
                captionStatus = document.video.captionStatus?.let { AssetStatus.valueOf(it) } ?: AssetStatus.UNKNOWN,
                downloadableVideoStatus = document.video.hasHDVideo
                    ?.let { hd -> if (hd) AssetStatus.AVAILABLE else AssetStatus.UNAVAILABLE }
                    ?: AssetStatus.UNKNOWN,
                playbackId = document.video.playbackId ?: "" //TODO: to be addressed after migration
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
