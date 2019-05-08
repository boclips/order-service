package com.boclips.terry.infrastructure

import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrdersRepository
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

class MongoOrdersRepository(private val uri: String) : OrdersRepository {
    val mongoClient : MongoClient = KMongo.createClient(MongoClientURI(uri))

    override fun add(order: Order) {
        mongoClient.getDatabase("order-service")
            .getCollection<OrderDocument>("orders")
            .insertOne(OrderDocument(id = ObjectId(order.id)))
    }

    override fun findAll(): List<Order> =
        mongoClient.getDatabase("order-service")
            .getCollection<OrderDocument>("orders")
            .find()
            .map { document -> Order(id = document.id.toHexString()) }
            .toList()
}
