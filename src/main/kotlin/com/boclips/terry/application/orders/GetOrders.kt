package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.presentation.orders.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrders(
    private val orderRepository: OrdersRepository
) {
    operator fun invoke() =
        orderRepository.findAll()
            .map { OrderResource.fromOrder(it) }
}
