package com.boclips.terry.domain

import com.boclips.terry.infrastructure.LegacyOrderDocument

interface OrdersRepository {
    fun add(order: Order, legacyDocument: LegacyOrderDocument): OrdersRepository
    fun documentForOrderId(orderId: String): LegacyOrderDocument?
    fun clear(): OrdersRepository
    fun findAll(): List<Order>
}
