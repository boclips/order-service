package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.Order
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "orders")
data class OrderResource(
    val id: String,
    val creatorEmail: String,
    val vendorEmail: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val items: List<OrderItemResource>
) {
    companion object {
        fun fromOrder(order: Order): OrderResource =
            OrderResource(
                id = order.id.value,
                creatorEmail = order.creatorEmail,
                vendorEmail = order.vendorEmail,
                createdAt = order.createdAt.toString(),
                updatedAt = order.updatedAt.toString(),
                status = order.status.toString(),
                items = order.items
                    .map { item ->
                        OrderItemResource(
                            uuid = item.uuid,
                            price = PriceResource.fromBigDecimal(item.price),
                            transcriptRequested = item.transcriptRequested,
                            contentPartner = ContentPartnerResource(
                                item.contentPartner.referenceId.value,
                                item.contentPartner.name
                            ),
                            video = VideoResource(
                                id = item.video.referenceId.value,
                                title = item.video.title,
                                type = item.video.type
                            )
                        )
                    }
            )
    }
}

data class OrderItemResource(
    val uuid: String,
    val price: PriceResource,
    val transcriptRequested: Boolean,
    val contentPartner: ContentPartnerResource,
    val video: VideoResource
)

