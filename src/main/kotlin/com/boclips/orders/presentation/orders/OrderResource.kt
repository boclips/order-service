package com.boclips.orders.presentation.orders

import com.boclips.orders.domain.model.Order
import com.boclips.orders.presentation.OrdersController
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "orders")
data class OrderResource(
    val id: String,
    val legacyOrderId: String,
    val userDetails: UserDetailsResource,
    val status: String,
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
                status = order.status.toString(),
                items = order.items
                    .map {
                        EntityModel(
                            OrderItemResource.fromOrderItem(it),
                            OrdersController.getUpdateOrderItemPriceLink(order.id.value, it.id),
                            OrdersController.getUpdateOrderItemLink(order.id.value, it.id)
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

