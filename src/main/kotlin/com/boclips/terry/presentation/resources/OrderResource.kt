package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.Order
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "orders")
data class OrderResource(
    val id: String,
    val creatorEmail: String,
    val vendorEmail: String
) {
    companion object {
        fun fromOrder(order: Order): OrderResource =
            OrderResource(
                id = order.id,
                creatorEmail = order.creatorEmail,
                vendorEmail = order.vendorEmail
            )
    }
}
