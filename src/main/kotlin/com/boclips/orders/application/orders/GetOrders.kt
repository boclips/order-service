package com.boclips.orders.application.orders

import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.presentation.orders.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrders(
    private val orderRepository: OrdersRepository
) {
    operator fun invoke() =
        orderRepository.findAll()
            .map { OrderResource.fromOrder(it) }
}
