package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.converters.csv.OrderFromRequestConverter
import com.boclips.orders.domain.model.CartUpdateCommand
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.infrastructure.carts.CartsRepository
import com.boclips.orders.presentation.PlaceOrderRequest
import org.springframework.stereotype.Component

@Component
class PlaceOrder(
    private val orderService: OrderService,
    private val orderConverter: OrderFromRequestConverter,
    private val cartsRepository: CartsRepository
) {
    operator fun invoke(orderRequest: PlaceOrderRequest): Order {
        val ordersResult = orderConverter.toOrder(orderRequest)
        return orderService.create(ordersResult)
            .also { cartsRepository.update(CartUpdateCommand.EmptyCart(UserId(orderRequest.user.id))) }
    }
}
