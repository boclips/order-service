package com.boclips.orders.presentation.orders

import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderSource
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder.getSelfOrderLink
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "orders")
data class OrderResource(
    val id: String,
    val searchableOrderId: String,
    val userDetails: UserDetailsResource,
    val status: OrderStatusResource,
    val createdAt: String,
    val updatedAt: String,
    val deliveredAt: String?,
    val isbnNumber: String?,
    val items: List<OrderItemResource>,
    val totalPrice: PriceResource,
    val throughPlatform: Boolean,
    val note: String?,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<LinkRelation, Link>?
) {
    companion object {
        fun fromOrder(order: Order): OrderResource =
            OrderResource(
                id = order.id.value,
                searchableOrderId = order.legacyOrderId ?: order.id.value,
                isbnNumber = order.isbnOrProductNumber,
                userDetails = UserDetailsResource.toResource(order),
                createdAt = order.createdAt.toString(),
                updatedAt = order.updatedAt.toString(),
                deliveredAt = order.deliveredAt?.toString(),
                status = when (order.status) {
                    OrderStatus.READY -> OrderStatusResource.READY
                    OrderStatus.DELIVERED -> OrderStatusResource.DELIVERED
                    OrderStatus.INCOMPLETED -> OrderStatusResource.INCOMPLETED
                    OrderStatus.IN_PROGRESS -> OrderStatusResource.IN_PROGRESS
                    OrderStatus.CANCELLED -> OrderStatusResource.CANCELLED
                    OrderStatus.INVALID -> OrderStatusResource.INVALID
                },
                items = order.items
                    .map {
                        OrderItemResource.fromOrderItem(it, order.id.value)
                    },
                totalPrice = PriceResource(
                    value = order.totalPrice,
                    currency = order.currency
                ),
                throughPlatform = order.orderSource == OrderSource.LEGACY,
                note = order.note,
                _links = resourceLink(order.id.value).map { it.rel to it }.toMap()
            )

        private fun resourceLink(orderId: String) =
            listOfNotNull(
                getSelfOrderLink(orderId)
            )
    }
}

