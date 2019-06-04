package com.boclips.terry.infrastructure.orders

import com.boclips.events.types.LegacyOrder
import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.domain.model.OrderId
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection

const val collection = "legacyOrders"

class MongoLegacyOrdersRepository(val mongoClient: MongoClient) : LegacyOrdersRepository {

    override fun add(legacyOrder: LegacyOrder) {
    }

    override fun findById(orderId: OrderId): LegacyOrderDocument {

    }
}