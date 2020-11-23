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
            items = cart.items.map { CartItemDocument(videoId = it.videoId.value) }
        )
    }

    fun toCart(document: CartDocument): Cart {
        return Cart(
            cartId = CartId(document.id.toHexString()),
            userId = UserId(document.userId),
            items = document.items.map { CartItem(videoId = VideoId(it.videoId)) }
        )
    }
}
