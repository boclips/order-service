package com.boclips.orders.domain.model

import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.model.cart.UserId

sealed class CartUpdateCommand(val userId: UserId) {
    class AddItem(userId: UserId, val cartItem: CartItem) : CartUpdateCommand(userId = userId)
    class EmptyCart(userId: UserId) : CartUpdateCommand(userId = userId)
    class UpdateNote(userId: UserId, val note: String) : CartUpdateCommand(userId = userId)
}
