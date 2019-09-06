package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.infrastructure.orders.converters.OrderDocumentConverter
import com.boclips.terry.infrastructure.orders.exceptions.OrderNotFoundException
import java.time.Instant

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

    override fun findOneByLegacyId(legacyOrderId: String): Order? {
        return orders.find { it.legacyOrderId == legacyOrderId }
    }

    override fun update(orderUpdateCommand: OrderUpdateCommand): Order {
        when (orderUpdateCommand) {
            is OrderUpdateCommand.ReplaceStatus -> {
                val index = orders.indexOfFirst { it.id == orderUpdateCommand.orderId }

                if (index != -1) {
                    orders[index] =
                        orders[index].copy(status = orderUpdateCommand.orderStatus, updatedAt = Instant.now())
                }
            }
        }

        return findOne(orderUpdateCommand.orderId) ?: throw OrderNotFoundException(orderUpdateCommand.orderId)
    }
}
