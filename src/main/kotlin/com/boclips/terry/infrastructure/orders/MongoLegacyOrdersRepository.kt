package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.domain.model.OrderId
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.deleteMany
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

class MongoLegacyOrdersRepository(uri: String) : LegacyOrdersRepository {
    private val mongoClient: com.mongodb.MongoClient = KMongo.createClient(MongoClientURI(uri))

    companion object {
        const val collectionName = "legacy-orders"
    }

    override fun add(document: LegacyOrderDocument): LegacyOrdersRepository = this.also {
        collection().insertOne(document)
    }

    override fun clear() = this.also { collection().deleteMany() }

    override fun findById(orderId: OrderId): LegacyOrderDocument? {
        return collection().findOne(OrderDocument::id eq ObjectId(orderId.value))
    }

    private fun collection(): MongoCollection<LegacyOrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<LegacyOrderDocument>(collectionName)
}