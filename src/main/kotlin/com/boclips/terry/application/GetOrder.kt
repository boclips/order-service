package com.boclips.terry.application

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.service.OrderService
import com.boclips.terry.presentation.resources.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrder(private val orderService: OrderService) {
    operator fun invoke(id: String): OrderResource {
        val order = orderService.findOrderById(OrderId(value = id))

        return OrderResource.fromOrder(order!!)
    }
}