package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.infrastructure.orders.converters.OrderDocumentConverter
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.deleteMany
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

const val databaseName = "order-service-db"

class MongoOrdersRepository(uri: String) : OrdersRepository {
    private val mongoClient: MongoClient = KMongo.createClient(MongoClientURI(uri))

    companion object {
        const val collectionName = "orders"
    }

    override fun add(order: Order) = this.also {
        collection()
            .insertOne(
                OrderDocumentConverter.toOrderDocument(order)
            )
    }

    override fun clear() = this.also { collection().deleteMany() }

    override fun findAll(): List<Order> =
        collection()
            .find()
            .map(OrderDocumentConverter::toOrder)
            .toList()

    override fun findOne(id: OrderId): Order? {
        return collection().findOne(OrderDocument::id eq ObjectId(id.value))?.let(
            OrderDocumentConverter::toOrder
        )
    }

    private fun collection(): MongoCollection<OrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<OrderDocument>(collectionName)
}
