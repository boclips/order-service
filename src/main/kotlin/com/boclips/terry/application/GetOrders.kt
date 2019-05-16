package com.boclips.terry.application

import com.boclips.terry.domain.OrderService
import com.boclips.terry.presentation.resources.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrders(
    private val orderService: OrderService
) {
    operator fun invoke(): List<OrderResource> {
        val orders = orderService.findAll()

        return orders.map { OrderResource.fromOrder(it) }
    }
}
