package com.boclips.terry.domain.service

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrdersRepository
import org.springframework.stereotype.Component

@Component
class OrderService(val ordersRepository: OrdersRepository) {
    fun createIfNonExistent(order: Order) {
        ordersRepository.findOneByLegacyId(order.legacyOrderId) ?: ordersRepository.save(order)
    }
}
