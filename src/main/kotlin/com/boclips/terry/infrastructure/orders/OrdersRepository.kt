package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.Order
import com.boclips.terry.infrastructure.LegacyOrderDocument

interface OrdersRepository {
    fun add(order: Order, legacyDocument: LegacyOrderDocument): OrdersRepository
    fun documentForOrderId(orderId: String): LegacyOrderDocument?
    fun clear(): OrdersRepository
    fun findAll(): List<Order>
}
