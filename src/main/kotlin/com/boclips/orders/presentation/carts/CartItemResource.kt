package com.boclips.orders.presentation.carts

import org.springframework.hateoas.Link

class CartItemResource(
    val id: String,
    val videoId: String,
    val _links: List<Link>
)
