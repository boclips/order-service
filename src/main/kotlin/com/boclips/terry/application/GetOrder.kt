package com.boclips.terry.application

import com.boclips.terry.domain.OrderService
import com.boclips.terry.presentation.resources.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrder(private val orderService: OrderService) {
    operator fun invoke(id: String): OrderResource {
        val order = orderService.findOrderById(id)

        return OrderResource.fromOrder(order!!)
    }
}