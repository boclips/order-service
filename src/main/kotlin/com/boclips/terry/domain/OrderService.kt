package com.boclips.terry.domain

import com.boclips.terry.infrastructure.orders.OrdersRepository
import org.springframework.stereotype.Component

@Component
class OrderService(private val repo: OrdersRepository) {
    fun findAll(): List<Order> = repo.findAll()
    fun findOrderById(id: String): Order? = repo.findOne(id)
}
