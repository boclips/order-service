package com.boclips.terry.infrastructure.legacyorders

import com.boclips.terry.domain.LegacyOrder
import com.boclips.terry.domain.LegacyOrdersRepository
import com.boclips.terry.infrastructure.LegacyOrderDocument
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.deleteMany
import org.litote.kmongo.getCollection

const val databaseName = "order-service"
const val collectionName = "legacy-orders"

class MongoLegacyOrdersRepository(private val uri: String) : LegacyOrdersRepository {
    val mongoClient: MongoClient = KMongo.createClient(MongoClientURI(uri))

    override fun add(item: LegacyOrder) = this.also {
        collection()
            .insertOne(LegacyOrderDocument(legacyId = ObjectId(item.legacyId)))
    }

    override fun clear() = this.also { collection().deleteMany() }

    override fun findAll(): List<LegacyOrder> =
        collection()
            .find()
            .map { document -> LegacyOrder(legacyId = document.legacyId.toString()) }
            .toList()

    private fun collection(): MongoCollection<LegacyOrderDocument> =
        mongoClient.getDatabase(databaseName).getCollection<LegacyOrderDocument>(collectionName)
}
