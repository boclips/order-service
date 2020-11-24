package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.presentation.hateos.CartsLinkBuilder

class CartItemsResource(
    val items: List<CartItemResource>
) {
    companion object {
        fun fromCartItems(cartItems: List<CartItem>) = CartItemsResource(
            items = cartItems.map {
                CartItemResource(
                    id = it.id,
                    videoId = it.videoId.value,
                    _links = listOfNotNull(CartsLinkBuilder.cartItemLink(it.id))
                )
            }
        )
    }
}
