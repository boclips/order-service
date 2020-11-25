package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.presentation.hateos.CartsLinkBuilder
import org.springframework.hateoas.EntityModel

class CartItemResource(
    val id: String,
    val videoId: String
) {
    companion object {
        fun fromCartItem(cartItem: CartItem) = EntityModel(
            CartItemResource(
                id = cartItem.id,
                videoId = cartItem.videoId.value
            ),
            listOfNotNull(CartsLinkBuilder.cartItemLink(cartItem.id))
        )
    }
}
