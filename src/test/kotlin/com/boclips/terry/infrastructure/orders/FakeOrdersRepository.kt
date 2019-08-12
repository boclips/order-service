package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.infrastructure.orders.converters.OrderDocumentConverter

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

    override fun add(order: Order) = this.also {
        if (order.id.value == "please-throw") {
            throw Exception("deliberately thrown in test")
        } else {
            orderDocuments.add(OrderDocumentConverter.toOrderDocument(order))

            orders.add(order)
        }
    }

    override fun findAll(): List<Order> = orders.sortedByDescending { it.updatedAt }

    override fun findOne(id: OrderId): Order? {
        return orders.find { it.id == id }
    }
}
