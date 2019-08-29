package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.TrimRequest

data class OrderItemResource(
    val uuid: String,
    val price: PriceResource,
    val transcriptRequested: Boolean,
    val contentPartner: ContentPartnerResource,
    val video: VideoResource,
    val trim: String?
) {
    companion object {
        fun fromOrderItem(item: OrderItem): OrderItemResource =
            OrderItemResource(
                uuid = item.uuid,
                price = PriceResource.fromBigDecimal(item.price),
                transcriptRequested = item.transcriptRequested,
                contentPartner = ContentPartnerResource(
                    item.contentPartner.referenceId.value,
                    item.contentPartner.name
                ),
                trim = when(item.trim) {
                    is TrimRequest.WithTrimming -> item.trim.label
                    TrimRequest.NoTrimming -> null
                },
                video = VideoResource(
                    id = item.video.referenceId.value,
                    title = item.video.title,
                    type = item.video.type,
                    videoReference = item.video.videoReference
                )
            )
    }
}
