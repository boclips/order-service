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
                            video = VideoResource(
                                id = item.video.id.value,
                                title = item.video.title,
                                source = item.video.source,
                                type = item.video.type.toString()
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
    val video: VideoResource
)

