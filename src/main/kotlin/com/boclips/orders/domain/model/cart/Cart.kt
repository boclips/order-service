package com.boclips.orders.domain.model.cart

data class Cart(
    val cartId: CartId,
    val userId: UserId,
    val items: List<CartItem> = emptyList()
)
