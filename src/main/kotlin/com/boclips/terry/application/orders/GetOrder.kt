package com.boclips.terry.application.orders

import com.boclips.terry.application.exceptions.InvalidOrderRequest
import com.boclips.terry.application.exceptions.OrderNotFoundException
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.presentation.resources.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrder(private val ordersRepository: OrdersRepository) {
    operator fun invoke(id: String?): OrderResource {
        return findOrder(id)?.let { OrderResource.fromOrder(it) }
            ?: throw OrderNotFoundException(OrderId(id.orEmpty()))
    }

    private fun findOrder(id: String?): Order? = id?.let {
        return ordersRepository.findOne(OrderId(value = it))
    } ?: throw InvalidOrderRequest()
}
