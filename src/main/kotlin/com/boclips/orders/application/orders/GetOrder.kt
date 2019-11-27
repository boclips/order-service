package com.boclips.orders.application.orders

import com.boclips.orders.application.exceptions.InvalidOrderRequest
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.presentation.orders.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrder(private val ordersRepository: OrdersRepository) {
    operator fun invoke(id: String?): OrderResource {
        return findOrder(id).let { OrderResource.fromOrder(it) }
    }

    private fun findOrder(id: String?): Order {
        if (id == null) {
            throw InvalidOrderRequest()
        }

        return ordersRepository.findOne(OrderId(value = id))
            ?: throw OrderNotFoundException(OrderId(id.orEmpty()))
    }
}
