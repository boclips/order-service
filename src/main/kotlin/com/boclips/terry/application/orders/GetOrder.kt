package com.boclips.terry.application.orders

import com.boclips.terry.application.exceptions.InvalidOrderRequest
import com.boclips.terry.application.exceptions.OrderNotFoundException
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.service.OrderService
import com.boclips.terry.presentation.resources.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrder(private val orderService: OrderService) {
    operator fun invoke(id: String?): OrderResource {
        return findOrder(id)?.let { OrderResource.fromOrder(it) }
            ?: throw OrderNotFoundException("Cannot find order for id: $id")
    }

    private fun findOrder(id: String?): Order? = id?.let {
        return orderService.findOrderById(OrderId(value = it))
    } ?: throw InvalidOrderRequest()
}
