package com.boclips.terry.presentation

import com.boclips.terry.domain.Order
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "orders")
data class OrderResource(val id: String) {
    companion object {
        fun fromOrder(order: Order): OrderResource = OrderResource(order.id)
    }
}
