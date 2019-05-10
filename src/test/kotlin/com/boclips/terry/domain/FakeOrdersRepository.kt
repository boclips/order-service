package com.boclips.terry.domain

import com.boclips.terry.infrastructure.LegacyOrderDocument
import com.boclips.terry.infrastructure.OrderDocument
import org.bson.types.ObjectId

class FakeOrdersRepository : OrdersRepository {
    lateinit var orderDocuments: MutableList<OrderDocument>
    lateinit var orders: MutableList<Order>

    init {
        clear()
    }

    override fun clear(): OrdersRepository = this.also {
        orderDocuments = mutableListOf()
        orders = mutableListOf()
    }

    override fun add(order: Order, legacyDocument: LegacyOrderDocument) = this.also {
        orderDocuments.add(OrderDocument(orderId = ObjectId(order.id), legacyDocument = legacyDocument))
        orders.add(order)
    }

    override fun findAll(): List<Order> = orders

    override fun documentForOrderId(orderId: String): LegacyOrderDocument? =
        orderDocuments
            .find { orderDocument -> orderDocument.orderId.toHexString() == orderId }
            ?.legacyDocument
}
