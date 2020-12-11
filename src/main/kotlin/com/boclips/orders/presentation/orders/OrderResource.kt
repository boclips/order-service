package com.boclips.orders.presentation.orders

import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder.getUpdateOrderItemLink
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder.getUpdateOrderItemPriceLink
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "orders")
data class OrderResource(
    val id: String,
    val legacyOrderId: String,
    val userDetails: UserDetailsResource,
    val status: OrderStatusResource,
    val createdAt: String,
    val updatedAt: String,
    val isbnNumber: String?,
    val items: List<EntityModel<OrderItemResource>>,
    val totalPrice: PriceResource,
    val throughPlatform: Boolean
) {
    companion object {
        fun fromOrder(order: Order): OrderResource =
            OrderResource(
                id = order.id.value,
                legacyOrderId = order.legacyOrderId,
                isbnNumber = order.isbnOrProductNumber,
                userDetails = UserDetailsResource.toResource(order),
                createdAt = order.createdAt.toString(),
                updatedAt = order.updatedAt.toString(),
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
                        EntityModel(
                            OrderItemResource.fromOrderItem(it),
                            getUpdateOrderItemPriceLink(order.id.value, it.id),
                            getUpdateOrderItemLink(order.id.value, it.id)
                        )
                    },
                totalPrice = PriceResource(
                    value = order.totalPrice,
                    currency = order.currency

                ),
                throughPlatform = order.isThroughPlatform
            )
    }
}

