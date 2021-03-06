package com.boclips.orders.presentation.carts

import org.springframework.hateoas.EntityModel

class CartResource(
    val note: String?,
    val items: List<EntityModel<CartItemResource>>
)
