package com.boclips.orders.infrastructure.carts

import com.boclips.orders.domain.model.CartUpdateCommand
import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.cart.UserId

interface CartsRepository {
    fun create(cart: Cart): Cart
    fun update(cartUpdateCommand: CartUpdateCommand): Cart
    fun findByUserId(userId: UserId): Cart?
    fun deleteAll()
    fun findAll(): List<Cart>
}