package com.boclips.orders.presentation.orders

import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItem
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation

data class OrderItemResource(
    val price: PriceResource?,
    val transcriptRequested: Boolean,
    val captionsRequested: Boolean,
    val editRequest: String?,
    val channel: ChannelResource,
    val video: VideoResource,
    val licenseDuration: String?,
    val licenseTerritory: String?,
    val trim: String?,
    val notes: String?,
    val id: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<LinkRelation, Link>?
) {
    companion object {
        fun fromOrderItem(item: OrderItem, orderId: String? = null): OrderItemResource =
            OrderItemResource(
                id = item.id,
                price = PriceResource.fromPrice(item.price),
                transcriptRequested = item.transcriptRequested,
                captionsRequested = item.captionsRequested,
                editRequest = item.editRequest,
                channel = ChannelResource(
                    item.video.channel.videoServiceId.value,
                    item.video.channel.name,
                    item.video.channel.currency.currencyCode
                ),
                trim = when (item.trim) {
                    is TrimRequest.WithTrimming -> item.trim.label
                    TrimRequest.NoTrimming -> null
                },
                video = VideoResource(
                    id = item.video.videoServiceId.value,
                    title = item.video.title,
                    types = item.video.types,
                    videoReference = item.video.channelVideoId,
                    maxResolutionAvailable = when (item.video.downloadableVideoStatus) {
                        AssetStatus.AVAILABLE -> true
                        else -> false
                    },
                    captionStatus = when (item.video.captionStatus) {
                        AssetStatus.AVAILABLE -> CaptionStatusResource.AVAILABLE
                        AssetStatus.REQUESTED -> CaptionStatusResource.REQUESTED
                        AssetStatus.PROCESSING -> CaptionStatusResource.PROCESSING
                        AssetStatus.UNAVAILABLE -> CaptionStatusResource.UNAVAILABLE
                        AssetStatus.UNKNOWN -> CaptionStatusResource.UNAVAILABLE
                    },
                    _links = mapOf(
                        "fullProjection" to Link(item.video.fullProjectionLink.toString(), "fullProjection"),
                        "videoUpload" to Link(item.video.videoUploadLink.toString(), "videoUpload"),
                        "captionAdmin" to Link(item.video.captionAdminLink.toString(), "captionAdmin")
                    )
                ),
                licenseDuration = item.license?.let(this::getDurationLabel),
                licenseTerritory = item.license?.territory,
                notes = item.notes,
                _links = orderId?.let { resourceLink(itemId = item.id, orderId = it).map { it.rel to it }.toMap() }
            )

        private fun resourceLink(orderId: String, itemId: String) =
            listOfNotNull(
                OrdersLinkBuilder.getUpdateOrderItemPriceLink(orderItemId = itemId, orderId = orderId),
                OrdersLinkBuilder.getUpdateOrderItemLink(orderItemId = itemId, orderId = orderId)
            )

        private fun getDurationLabel(license: OrderItemLicense): String? {
            return when (license.duration) {
                is Duration.Time -> {
                    val unit = license.duration.unit.name.toLowerCase().capitalize().let {
                        if (license.duration.amount > 1) {
                            it
                        } else {
                            it.removeSuffix("s")
                        }
                    }

                    "${license.duration.amount} $unit"
                }
                is Duration.Description -> license.duration.label
                null -> null
            }
        }
    }
}
