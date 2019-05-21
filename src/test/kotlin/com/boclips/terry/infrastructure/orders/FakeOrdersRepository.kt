package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.Order
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
        if (order.id == "please-throw") {
            throw Exception("deliberately thrown in test")
        } else {
            orderDocuments.add(
                OrderDocument(
                    id = ObjectId(order.id),
                    uuid = order.uuid,
                    status = order.status.toString(),
                    isbnOrProductNumber = order.isbnOrProductNumber,
                    creator = order.creator,
                    vendor = order.vendor,
                    updatedAt = order.updatedAt,
                    createdAt = order.createdAt,
                    legacyDocument = legacyDocument
                )
            )
            orders.add(order)
        }
    }

    override fun findAll(): List<Order> = orders

    override fun documentForOrderId(orderId: String): LegacyOrderDocument? =
        orderDocuments
            .find { orderDocument -> orderDocument.id.toHexString() == orderId }
            ?.legacyDocument
}