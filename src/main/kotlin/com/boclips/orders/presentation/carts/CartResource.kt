package com.boclips.orders.presentation.carts

import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.presentation.hateos.CartsLinkBuilder
import org.springframework.hateoas.Link

class CartResource(
    val userId: String,
    val items: List<CartItemResource>,
    val _links: List<Link>
) {
    companion object {
        fun fromCart(cart: Cart) = CartResource(
            userId = cart.userId.value,
            items = CartItemsResource.fromCartItems(cart.items).items,
            _links = listOfNotNull(CartsLinkBuilder.cartSelfLink(), CartsLinkBuilder.addItemToCartLink())
        )
    }
}
