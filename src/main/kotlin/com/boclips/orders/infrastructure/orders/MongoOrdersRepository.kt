package com.boclips.orders.infrastructure.orders

import com.boclips.orders.common.Do
import com.boclips.orders.domain.exceptions.OrderItemNotFoundException
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.model.Price
import com.boclips.orders.infrastructure.orders.converters.OrderDocumentConverter
import com.boclips.orders.infrastructure.orders.converters.OrderItemDocumentConverter
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import org.bson.types.ObjectId
import org.litote.kmongo.SetTo
import org.litote.kmongo.deleteMany
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.orderBy
import org.litote.kmongo.set
import java.time.Instant

const val databaseName = "order-service-db"

class MongoOrdersRepository(private val mongoClient: MongoClient) : OrdersRepository {

    companion object {
        const val collectionName = "orders"
    }

    override fun save(order: Order) =
        collection()
            .insertOne(
                OrderDocumentConverter.toOrderDocument(order)
            ).let { this.findOne(order.id) }!!

    override fun deleteAll() {
        collection().deleteMany()
    }

    override fun findAll(): List<Order> =
        collection()
            .find()
            .sort(orderBy(OrderDocument::updatedAt, ascending = false))
            .map(OrderDocumentConverter::toOrder)
            .toList()

    override fun findOne(id: OrderId): Order? =
        id.value
            .takeIf { ObjectId.isValid(it) }
            ?.let { collection().findOne(OrderDocument::id eq ObjectId(it)) }
            ?.let(OrderDocumentConverter::toOrder)

    override fun findOneByLegacyId(legacyOrderId: String): Order? {
        return collection().findOne(OrderDocument::legacyOrderId eq legacyOrderId)?.let(
            OrderDocumentConverter::toOrder
        )
    }

    override fun update(orderUpdateCommand: OrderUpdateCommand): Order {
        if (!ObjectId.isValid(orderUpdateCommand.orderId.value)) {
            throw OrderNotFoundException(orderUpdateCommand.orderId)
        }

        Do exhaustive when (orderUpdateCommand) {
            is OrderUpdateCommand.SetOrderCancellation -> collection().updateOne(
                OrderDocument::id eq ObjectId(orderUpdateCommand.orderId.value),
                set(OrderDocument::cancelled, orderUpdateCommand.cancelled)
            )
            is OrderUpdateCommand.UpdateOrderCurrency -> collection().updateOne(
                OrderDocument::id eq ObjectId(orderUpdateCommand.orderId.value),
                set(
                    SetTo(OrderDocument::currency, orderUpdateCommand.currency),
                    SetTo(OrderDocument::fxRateToGbp, orderUpdateCommand.fxRateToGbp)
                )
            )
            is OrderUpdateCommand.OrderItemUpdateCommand -> findOne(orderUpdateCommand.orderId)?.let {
                updateOrderItems(retrievedOrder = it, updateCommand = orderUpdateCommand)
            }
        }

        collection().updateOne(
            OrderDocument::id eq ObjectId(orderUpdateCommand.orderId.value),
            set(OrderDocument::updatedAt, Instant.now())
        )

        return findOne(orderUpdateCommand.orderId) ?: throw OrderNotFoundException(
            orderUpdateCommand.orderId
        )
    }

    private fun updateOrderItems(retrievedOrder: Order, updateCommand: OrderUpdateCommand.OrderItemUpdateCommand) {
        if (retrievedOrder.items.firstOrNull { it.id == updateCommand.orderItemsId } == null) {
            throw OrderItemNotFoundException(orderId = retrievedOrder.id, orderItemId = updateCommand.orderItemsId)
        }

        val updatedItems = retrievedOrder.items.map { orderItem ->
            if (orderItem.id == updateCommand.orderItemsId) {
                when (updateCommand) {
                    is OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemPrice -> orderItem.copy(
                        price = Price(
                            amount = updateCommand.amount,
                            currency = orderItem.price.currency
                        )
                    )
                    is OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemLicense -> orderItem.copy(license = updateCommand.orderItemLicense)
                }
            } else {
                orderItem
            }
        }

        collection().updateOne(
            OrderDocument::id eq ObjectId(updateCommand.orderId.value),
            set(OrderDocument::items, updatedItems.map { OrderItemDocumentConverter.toOrderItemDocument(it) })
        )
    }

    private fun collection(): MongoCollection<OrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<OrderDocument>(collectionName)
}
