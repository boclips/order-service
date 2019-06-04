package com.boclips.terry.domain.model

import com.boclips.terry.infrastructure.orders.LegacyOrderDocument

interface OrdersRepository {
    fun add(order: Order, legacyDocument: LegacyOrderDocument): OrdersRepository
    fun documentForOrderId(orderId: String): LegacyOrderDocument?
    fun clear(): OrdersRepository
    fun findAll(): List<Order>
    fun findOne(id: String): Order?
}
