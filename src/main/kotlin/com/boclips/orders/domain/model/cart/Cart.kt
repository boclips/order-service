package com.boclips.orders.domain.model.cart

data class Cart(
    val cartId: CartId,
    val userId: UserId,
    val note: String? = null,
    val items: List<CartItem> = emptyList()
)
