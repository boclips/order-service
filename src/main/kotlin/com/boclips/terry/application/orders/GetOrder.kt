package com.boclips.terry.application.orders

import com.boclips.terry.application.exceptions.InvalidOrderRequest
import com.boclips.terry.domain.exceptions.OrderNotFoundException
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.presentation.orders.OrderResource
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
