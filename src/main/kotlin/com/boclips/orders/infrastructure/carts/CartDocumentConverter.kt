package com.boclips.orders.infrastructure.carts

import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartId
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.cart.UserId
import com.boclips.orders.domain.model.video.VideoId
import org.bson.types.ObjectId

object CartDocumentConverter {
    fun toCartDocument(cart: Cart): CartDocument {
        return CartDocument(
            id = ObjectId(cart.cartId.value),
            userId = cart.userId.value,
            items = cart.items.map { cartItemToCartItemDocument(it) }
        )
    }

    fun fromDocument(document: CartDocument): Cart {
        return Cart(
            cartId = CartId(document.id.toHexString()),
            userId = UserId(document.userId),
            items = document.items.map { cartItemDocumentToCartItem(it) }
        )
    }

    fun cartItemToCartItemDocument(cartItem: CartItem): CartItemDocument =
        CartItemDocument(
            id = cartItem.id,
            videoId = cartItem.videoId.value
        )

    private fun cartItemDocumentToCartItem(document: CartItemDocument): CartItem =
        CartItem(
            id = document.id,
            videoId = VideoId(document.videoId)
        )
}