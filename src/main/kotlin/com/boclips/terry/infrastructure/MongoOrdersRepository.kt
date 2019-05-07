package com.boclips.terry.infrastructure

import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrdersRepository
import com.mongodb.MongoClient
import org.bson.types.ObjectId
import org.litote.kmongo.getCollection

class MongoOrdersRepository(private val mongoClient: MongoClient) : OrdersRepository {
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
