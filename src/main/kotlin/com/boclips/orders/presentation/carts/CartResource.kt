package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.Cart
import org.springframework.hateoas.EntityModel

class CartResource(
    val userId: String,
    val items: List<EntityModel<CartItemResource>>
) {
    companion object {
        fun fromCart(cart: Cart) = CartResource(
            userId = cart.userId.value,
            items = cart.items.map { CartItemResource.fromCartItem(it) }
        )
    }
}
