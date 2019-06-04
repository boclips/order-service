package com.boclips.terry.domain.service

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrdersRepository
import org.springframework.stereotype.Component

@Component
class OrderService(private val repo: OrdersRepository) {
    fun findAll(): List<Order> = repo.findAll()
    fun findOrderById(id: String): Order? = repo.findOne(id)
}
