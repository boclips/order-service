package com.boclips.orders.infrastructure.carts

import com.boclips.orders.domain.exceptions.CartItemNotFoundException
import com.boclips.orders.domain.model.CartUpdateCommand
import com.boclips.orders.domain.model.cart.AdditionalServices
import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartItemDocumentId
import com.boclips.orders.domain.model.cart.UserId
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import mu.KLogging
import org.litote.kmongo.*
import kotlin.collections.toList

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

    override fun update(cartUpdateCommand: CartUpdateCommand): Cart {
        val updateBson = when (cartUpdateCommand) {
            is CartUpdateCommand.AddItem -> push(
                CartDocument::items,
                CartDocumentConverter.cartItemToCartItemDocument(cartUpdateCommand.cartItem)
            )
            is CartUpdateCommand.EmptyCart -> combine(
                set(CartDocument::items, emptyList()),
                set(CartDocument::note, null)
            )
            is CartUpdateCommand.UpdateNote -> set(
                CartDocument::note,
                cartUpdateCommand.note
            )
        }

        updateBson.let {
            collection().updateOne(
                CartDocument::userId eq cartUpdateCommand.userId.value,
                it
            )
        }

        return findByUserId(cartUpdateCommand.userId)
            ?: throw IllegalStateException("Adding cart items: cart does not exist for user: ${cartUpdateCommand.userId}")
    }

    override fun updateCartItem(
        userId: UserId,
        cartItemId: String,
        additionalServices: AdditionalServices
    ): Cart? {
        collection().findOne(
            and(
                CartDocument::userId eq userId.value,
                CartDocument::items / CartItemDocument::id eq cartItemId
            )
        ) ?: throw CartItemNotFoundException(cartItemId)

        collection().updateOne(
            and(CartDocument::userId eq userId.value, CartDocument::items / CartItemDocument::id eq cartItemId),
            set(
                CartDocument::items.colProperty.posOp / CartItemDocument::additionalServices,
                CartDocumentConverter.additionalServicesDocument(additionalServices)
            )
        )

        return findByUserId(userId)
    }

    override fun deleteItem(userId: UserId, cartItemId: String): Boolean {
        return collection().updateOne(
            CartDocument::userId eq userId.value,
            pull(
                CartDocument::items,
                CartItemDocumentId(cartItemId)
            )
        ).modifiedCount > 0
    }

    override fun findByUserId(userId: UserId): Cart? {
        return collection().findOne(CartDocument::userId eq userId.value)?.let(
            CartDocumentConverter::fromDocument
        )
    }

    override fun findAll(): List<Cart> {
        return collection().find().map(CartDocumentConverter::fromDocument).toList()
    }

    override fun deleteAll() {
        collection().deleteMany()
    }

    private fun collection(): MongoCollection<CartDocument> =
        mongoClient.getDatabase(databaseName).getCollection<CartDocument>(collectionName)
}
