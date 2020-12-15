package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.converters.csv.OrderFromRequestConverter
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.presentation.PlaceOrderRequest
import org.springframework.stereotype.Component

@Component
class PlaceOrder(
    private val orderService: OrderService,
    private val orderConverter: OrderFromRequestConverter
) {
    operator fun invoke(orderRequest: PlaceOrderRequest): Order {
        val ordersResult = orderConverter.toOrder(orderRequest)
        return orderService.createIfNonExistent(ordersResult)
    }
}
