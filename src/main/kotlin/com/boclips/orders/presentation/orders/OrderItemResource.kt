package com.boclips.orders.presentation.orders

import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItem
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest

data class OrderItemResource(
    val price: PriceResource?,
    val transcriptRequested: Boolean,
    val contentPartner: ContentPartnerResource,
    val video: VideoResource,
    val licenseDuration: String?,
    val licenseTerritory: String?,
    val trim: String?,
    val notes: String?,
    val id: String
) {
    companion object {
        fun fromOrderItem(item: OrderItem): OrderItemResource =
            OrderItemResource(
                id = item.id,
                price = PriceResource.fromPrice(item.price),
                transcriptRequested = item.transcriptRequested,
                contentPartner = ContentPartnerResource(
                    item.video.contentPartner.videoServiceId.value,
                    item.video.contentPartner.name,
                    item.video.contentPartner.currency.currencyCode
                ),
                trim = when (item.trim) {
                    is TrimRequest.WithTrimming -> item.trim.label
                    TrimRequest.NoTrimming -> null
                },
                video = VideoResource(
                    id = item.video.videoServiceId.value,
                    title = item.video.title,
                    type = item.video.type,
                    videoReference = item.video.contentPartnerVideoId
                ),
                licenseDuration = item.license?.let(this::getDurationLabel),
                licenseTerritory = item.license?.territory,
                notes = item.notes
            )

        private fun getDurationLabel(license: OrderItemLicense): String {
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
            }
        }
    }
}
