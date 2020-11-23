package com.boclips.orders.infrastructure.carts

import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.cart.UserId

interface CartsRepository {
    fun create(cart: Cart): Cart
    fun update(userId: UserId, cartItem: CartItem): Cart
    fun findByUserId(userId: UserId): Cart?
}