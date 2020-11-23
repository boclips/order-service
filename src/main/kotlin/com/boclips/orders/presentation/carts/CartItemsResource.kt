package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.CartItem

class CartItemsResource(
    val items: List<CartItemResource>
) {
    companion object {
        fun fromCartItems(cartItems: List<CartItem>) = CartItemsResource(
            items = cartItems.map {
                CartItemResource(
                    id = it.id,
                    videoId = it.videoId.value
                )
            }
        )
    }
}
