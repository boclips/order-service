package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.Cart

class CartResource(
    val userId: String,
    val items: List<CartItemResource>
) {
    companion object {
        fun fromCart(cart: Cart) = CartResource(
            userId = cart.userId.value,
            items = cart.items.map {
                CartItemResource(
                    id = it.id,
                    videoId = it.videoId.value
                )
            }
        )
    }
}
