package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.Order
import com.boclips.terry.infrastructure.LegacyOrderDocument
import com.boclips.terry.infrastructure.OrderDocument
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
const val collectionName = "orders"

class MongoOrdersRepository(uri: String) : OrdersRepository {
    private val mongoClient: MongoClient = KMongo.createClient(MongoClientURI(uri))

    override fun add(order: Order, legacyDocument: LegacyOrderDocument) = this.also {
        collection()
            .insertOne(
                OrderDocument(
                    id = ObjectId(order.id),
                    uuid = order.uuid,
                    status = order.status.toString(),
                    isbnOrProductNumber = order.isbnOrProductNumber,
                    creatorEmail = order.creatorEmail,
                    vendorEmail = order.vendorEmail,
                    updatedAt = order.updatedAt,
                    createdAt = order.createdAt,
                    legacyDocument = legacyDocument
                )
            )
    }

    override fun clear() = this.also { collection().deleteMany() }

    override fun findAll(): List<Order> =
        collection()
            .find()
            .map(OrderDocument::toOrder)
            .toList()

    override fun documentForOrderId(orderId: String): LegacyOrderDocument? =
        collection()
            .findOne(OrderDocument::id eq ObjectId(orderId))
            ?.legacyDocument

    private fun collection(): MongoCollection<OrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<OrderDocument>(collectionName)
}
