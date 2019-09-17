package com.boclips.terry.domain.service

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrdersRepository
import org.springframework.stereotype.Component

@Component
class OrderService(val ordersRepository: OrdersRepository) {
    fun create(order: Order) {
        val doesNotExistByLegacyId = ordersRepository.findOneByLegacyId(order.legacyOrderId) == null

        if (doesNotExistByLegacyId) {
            ordersRepository.add(order)
        }
    }
}
