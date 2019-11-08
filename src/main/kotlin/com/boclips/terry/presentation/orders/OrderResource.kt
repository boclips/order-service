package com.boclips.terry.presentation.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.presentation.OrdersController
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "orders")
data class OrderResource(
    val id: String,
    val legacyOrderId: String,
    val userDetails: UserDetailsResource,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val isbnNumber: String?,
    val items: List<Resource<OrderItemResource>>,
    val totalPrice: PriceResource,
    val isThroughPlatform: Boolean
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
                        Resource(
                            OrderItemResource.fromOrderItem(it),
                            OrdersController.getUpdateOrderItemPriceLink(order.id.value, it.id),
                            OrdersController.getUpdateOrderItemLink(order.id.value, it.id)
                        )
                    },
                totalPrice = PriceResource(
                    value = order.totalPrice,
                    currency = order.currency

                ),
                isThroughPlatform = order.isThroughPlatform
            )
    }
}

