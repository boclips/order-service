package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.Order
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "orders")
data class OrderResource(
    val id: String,
    val orderProviderId: String,
    val creatorEmail: String,
    val vendorEmail: String,
    val userDetails: UserDetailsResource,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val items: List<OrderItemResource>
) {
    companion object {
        fun fromOrder(order: Order): OrderResource =
            OrderResource(
                id = order.id.value,
                orderProviderId = order.orderProviderId,
                creatorEmail = order.requestingUser.email,
                vendorEmail = order.authorisingUser.email,
                userDetails = UserDetailsResource.toResource(order),
                createdAt = order.createdAt.toString(),
                updatedAt = order.updatedAt.toString(),
                status = order.status.toString(),
                items = order.items
                    .map(OrderItemResource.Companion::fromOrderItem)
            )
    }
}

