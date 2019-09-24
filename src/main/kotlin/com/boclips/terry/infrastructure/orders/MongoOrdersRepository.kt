package com.boclips.terry.infrastructure.orders

import com.boclips.terry.common.Do
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.infrastructure.orders.converters.OrderDocumentConverter
import com.boclips.terry.domain.exceptions.OrderNotFoundException
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.deleteMany
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.orderBy
import org.litote.kmongo.set
import org.litote.kmongo.updateOne
import java.time.Instant

const val databaseName = "order-service-db"

class MongoOrdersRepository(uri: String) : OrdersRepository {

    private val mongoClient: MongoClient = KMongo.createClient(MongoClientURI(uri))

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
            is OrderUpdateCommand.ReplaceStatus -> collection().updateOne(
                OrderDocument::id eq ObjectId(orderUpdateCommand.orderId.value),
                set(OrderDocument::status, orderUpdateCommand.orderStatus.toString())
            )
            is OrderUpdateCommand.UpdateOrderItemsCurrency -> collection().updateOne(
                OrderDocument::id eq ObjectId(orderUpdateCommand.orderId.value),
                "{\$set:{'items.\$[].currency': '${orderUpdateCommand.currency}'}}"
            )
        }

        collection().updateOne(
            OrderDocument::id eq ObjectId(orderUpdateCommand.orderId.value),
            set(OrderDocument::updatedAt, Instant.now())
        )

        return findOne(orderUpdateCommand.orderId) ?: throw OrderNotFoundException(
            orderUpdateCommand.orderId
        )
    }

    private fun collection(): MongoCollection<OrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<OrderDocument>(collectionName)
}
