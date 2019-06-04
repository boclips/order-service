package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrdersRepository
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
                    id = ObjectId(order.id.value),
                    uuid = order.uuid,
                    status = order.status.toString(),
                    vendorEmail = order.vendorEmail,
                    creatorEmail = order.creatorEmail,
                    updatedAt = order.updatedAt,
                    createdAt = order.createdAt,
                    isbnOrProductNumber = order.isbnOrProductNumber,
                    legacyDocument = legacyDocument,
                    items = legacyDocument.items
                        .map { item ->
                            OrderItemDocument(
                                uuid = item.uuid,
                                price = item.price,
                                transcriptRequested = item.transcriptsRequired
                            )
                        }
                )
            )
    }

    override fun clear() = this.also { collection().deleteMany() }

    override fun findAll(): List<Order> =
        collection()
            .find()
            .map(OrderDocument::toOrder)
            .toList()

    override fun documentForOrderId(orderId: OrderId): LegacyOrderDocument? =
        collection()
            .findOne(OrderDocument::id eq ObjectId(orderId.value))
            ?.legacyDocument

    override fun findOne(id: OrderId): Order? {
        return collection().findOne(OrderDocument::id eq ObjectId(id.value))?.toOrder()
    }

    private fun collection(): MongoCollection<OrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<OrderDocument>(collectionName)
}
