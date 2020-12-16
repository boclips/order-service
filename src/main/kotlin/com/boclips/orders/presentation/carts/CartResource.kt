package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.Cart
import org.springframework.hateoas.EntityModel

class CartResource(
    val items: List<EntityModel<CartItemResource>>
) {
    companion object {
        fun fromCart(cart: Cart) = CartResource(
            items = cart.items.map { CartItemResource.fromCartItem(it) }
        )
    }
}
