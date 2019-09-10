package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest

data class OrderItemResource(
    val price: PriceResource,
    val transcriptRequested: Boolean,
    val contentPartner: ContentPartnerResource,
    val video: VideoResource,
    val licenseDuration: String,
    val licenseTerritory: String,
    val trim: String?
) {
    companion object {
        fun fromOrderItem(item: OrderItem): OrderItemResource =
            OrderItemResource(
                price = PriceResource.fromBigDecimal(item.price),
                transcriptRequested = item.transcriptRequested,
                contentPartner = ContentPartnerResource(
                    item.contentPartner.videoServiceId.value,
                    item.contentPartner.name
                ),
                trim = when (item.trim) {
                    is TrimRequest.WithTrimming -> item.trim.label
                    TrimRequest.NoTrimming -> null
                },
                video = VideoResource(
                    id = item.video.videoServiceId.value,
                    title = item.video.title,
                    type = item.video.type,
                    videoReference = item.video.videoReference
                ),
                licenseDuration = getDurationLabel(item.license),
                licenseTerritory = item.license.territory
            )

        private fun getDurationLabel(license: OrderItemLicense): String {
            val unit = license.duration.unit.name.toLowerCase().capitalize().let {
                if (license.duration.amount > 1) {
                    it
                } else {
                    it.removeSuffix("s")
                }
            }

            return "${license.duration.amount} $unit"
        }
    }
}
