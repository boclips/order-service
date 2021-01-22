package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.cart.UserId

class CartItemNotFoundException(cartItemId: String, userId: UserId) : BoclipsException(
    "Could not find cart item with ID: $cartItemId for user: ${userId.value}"
)
