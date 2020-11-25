package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.presentation.hateos.CartsLinkBuilder
import org.springframework.hateoas.Link

class CartItemResource(
    val id: String,
    val videoId: String,
    val _links: List<Link>
) {
    companion object {
        fun fromCartItem(cartItem: CartItem) = CartItemResource(
            id = cartItem.id,
            videoId = cartItem.videoId.value,
            _links = listOfNotNull(CartsLinkBuilder.cartItemLink(cartItem.id))
        )
    }
}
