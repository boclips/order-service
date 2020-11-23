package com.boclips.orders.infrastructure.carts

import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.cart.UserId
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.litote.kmongo.getCollection

const val databaseName = "order-service-db"

class MongoCartsRepository(private val mongoClient: MongoClient) : CartsRepository {
    companion object : KLogging() {
        const val collectionName = "carts"
    }

    override fun create(cart: Cart): Cart {
        return collection().insertOne(
            CartDocumentConverter.toCartDocument(cart)
        ).let { this.findByUserId(cart.userId) } ?: throw IllegalStateException("Can't create new cart")
    }

    override fun update(userId: UserId, cartItem: CartItem): Cart {
        TODO("Not yet implemented")
    }

    override fun findByUserId(userId: UserId): Cart? {
        TODO("Not yet implemented")
    }

    private fun collection(): MongoCollection<CartDocument> =
        mongoClient.getDatabase(databaseName).getCollection<CartDocument>(collectionName)
}
