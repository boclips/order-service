package com.boclips.orders.domain.model

interface OrdersRepository {
    fun save(order: Order): Order
    fun deleteAll()
    fun findAll(): List<Order>
    fun findOne(id: OrderId): Order?
    fun findOneByLegacyId(legacyOrderId: String): Order?
    fun update(orderUpdateCommand: OrderUpdateCommand): Order
    fun bulkUpdate(orderUpdateCommands: List<OrderUpdateCommand>)
    fun streamAll(filter: OrderFilter, consumer: (orders: Sequence<Order>) -> Unit)
}
