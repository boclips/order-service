package com.boclips.orders.domain.exceptions

class CartItemNotFoundException(cartItemId: String) : BoclipsException(
    "Could not find cart item with ID: $cartItemId"
)
