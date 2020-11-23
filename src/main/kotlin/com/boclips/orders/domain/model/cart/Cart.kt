package com.boclips.orders.domain.model.cart

data class Cart(
    val items: List<CartItem>,
    val cartId: CartId,
    val userId: UserId
)
