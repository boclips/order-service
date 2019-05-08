package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrdersRepository
import com.boclips.terry.infrastructure.OrderDocument
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

const val databaseName = "order-service"
const val collectionName = "orders"

class MongoOrdersRepository(private val uri: String) : OrdersRepository {
    val mongoClient : MongoClient = KMongo.createClient(MongoClientURI(uri))

    override fun add(order: Order) =
        collection()
            .insertOne(OrderDocument(id = ObjectId(order.id)))

    override fun findAll(): List<Order> =
        collection()
            .find()
            .map { document -> Order(id = document.id.toHexString()) }
            .toList()

    private fun collection() : MongoCollection<OrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<OrderDocument>(collectionName)
}
