package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrdersRepository
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

    override fun add(order: Order) = this.also {
        if (order.id.value == "please-throw") {
            throw Exception("deliberately thrown in test")
        } else {
            orderDocuments.add(
                OrderDocument(
                    id = ObjectId(order.id.value),
                    uuid = order.uuid,
                    status = order.status.toString(),
                    vendorEmail = order.vendorEmail,
                    creatorEmail = order.creatorEmail,
                    updatedAt = order.updatedAt,
                    createdAt = order.createdAt,
                    isbnOrProductNumber = order.isbnOrProductNumber,
                    items = order.items
                        .map { item ->
                            OrderItemDocument(
                                uuid = item.uuid,
                                price = item.price,
                                transcriptRequested = item.transcriptRequested
                            )
                        }
                )
            )
            orders.add(order)
        }
    }

    override fun findAll(): List<Order> = orders

    override fun findOne(id: OrderId): Order? {
        return orders.find { it.id == id }
    }
}
