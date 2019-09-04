package com.boclips.terry.domain.model

interface OrdersRepository {
    fun add(order: Order): OrdersRepository
    fun clear(): OrdersRepository
    fun findAll(): List<Order>
    fun findOne(id: OrderId): Order?
    fun findOneByLegacyId(legacyOrderId: String): Order?
    fun update(orderUpdateCommand: OrderUpdateCommand): Order
}
