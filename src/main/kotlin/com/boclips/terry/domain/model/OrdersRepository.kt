package com.boclips.terry.domain.model

interface OrdersRepository {
    fun save(order: Order): Order
    fun deleteAll()
    fun findAll(): List<Order>
    fun findOne(id: OrderId): Order?
    fun findOneByLegacyId(legacyOrderId: String): Order?
    fun update(orderUpdateCommand: OrderUpdateCommand): Order
}
